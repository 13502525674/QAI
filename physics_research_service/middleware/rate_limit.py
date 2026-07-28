"""
LLM 调用 4 层限流 — P0-4 优化

Layer 1: IP 级限流 (防 DDoS) — SlowAPI
Layer 2: 用户级限流 (基于 JWT user_id) — Redis 计数器
Layer 3: 成本级限流 (Token 配额) — Redis 累加
Layer 4: 并发级限流 (全局信号量) — asyncio.Semaphore

使用方法:
    # 在 routes.py 中
    from middleware.rate_limit import rate_limit_dep, with_concurrency_limit

    @router.post("/literature-review/stream")
    @limiter.limit("10/minute")  # Layer 1
    async def literature_review_stream(
        request: Request,
        body: ResearchRequest,
        user: dict = Depends(rate_limit_dep),  # Layer 2 + 3
    ):
        async def event_stream():
            async for event in await with_concurrency_limit(  # Layer 4
                engine.execute_stream(...)
            ):
                yield ...
"""
import asyncio
import time
import logging
from typing import Optional

from fastapi import HTTPException, Request, Depends, status

logger = logging.getLogger(__name__)

# Layer 1: IP 级限流 (slowapi 可选)
try:
    from slowapi import Limiter
    from slowapi.util import get_remote_address
    limiter = Limiter(key_func=get_remote_address)
    SLOWAPI_AVAILABLE = True
except ImportError:
    SLOWAPI_AVAILABLE = False
    limiter = None
    logger.info("[限流] slowapi 未安装，Layer 1 IP 限流不可用。安装: pip install slowapi")

# Layer 4: 全局并发信号量（FastAPI 服务最多 20 个并发 LLM 调用）
LLM_SEMAPHORE = asyncio.Semaphore(20)


# ============================================================
# Layer 2: 用户级限流 (Redis 计数器)
# ============================================================

class UserRateLimiter:
    """用户级限流 — 基于 Redis 计数器"""

    def __init__(self, redis=None):
        self.redis = redis

    async def check(self, user_id: str, max_per_hour: int = 20):
        """检查用户每小时请求限制

        Args:
            user_id: 用户 ID
            max_per_hour: 每小时最大请求数（默认 20）
        """
        if self.redis is None:
            # Redis 不可用时跳过限流（fail-open）
            return

        key = f"rl:user:{user_id}:hour:{int(time.time() // 3600)}"
        try:
            current = await self.redis.incr(key)
            if current == 1:
                await self.redis.expire(key, 3600)
            if current > max_per_hour:
                logger.warning(f"[限流] 用户 {user_id} 超时小时限制: {current}/{max_per_hour}")
                raise HTTPException(
                    status_code=429,
                    detail=f"用户请求过于频繁（{max_per_hour}/小时），请稍后再试",
                )
        except HTTPException:
            raise
        except Exception as e:
            logger.warning(f"[限流] 用户级限流检查失败，fail-open: {e}")


# ============================================================
# Layer 3: 成本级限流 (Token 配额)
# ============================================================

class TokenRateLimiter:
    """成本级限流 — 按每日 Token 消耗限制"""

    def __init__(self, redis=None, daily_quota: int = 100000):
        self.redis = redis
        self.daily_quota = daily_quota

    async def check_and_deduct(self, user_id: str, estimated_tokens: int):
        """检查并扣除 Token 配额

        Args:
            user_id: 用户 ID
            estimated_tokens: 估算的 Token 消耗
        """
        if self.redis is None:
            return

        key = f"rl:token:{user_id}:{time.strftime('%Y%m%d')}"
        try:
            current = int(await self.redis.get(key) or 0)
            if current + estimated_tokens > self.daily_quota:
                logger.warning(
                    f"[限流] 用户 {user_id} Token 配额不足: "
                    f"{current}/{self.daily_quota} (需要 {estimated_tokens})"
                )
                raise HTTPException(
                    status_code=429,
                    detail=f"今日 AI Token 额度已用完（{self.daily_quota}），明日重置",
                )
            await self.redis.incrby(key, estimated_tokens)
            if current == 0:
                await self.redis.expire(key, 86400)
        except HTTPException:
            raise
        except Exception as e:
            logger.warning(f"[限流] Token 限流检查失败，fail-open: {e}")


# ============================================================
# Layer 4: 并发级限流 (asyncio.Semaphore)
# ============================================================

async def with_concurrency_limit(coro):
    """包装协程，限制全局并发数

    用法:
        result = await with_concurrency_limit(some_async_call(...))
    """
    async with LLM_SEMAPHORE:
        return await coro


# ============================================================
# 组合依赖：Layer 2 + Layer 3
# ============================================================

async def rate_limit_dep(
    request: Request,
) -> dict:
    """限流依赖 — Layer 2 (用户级) + Layer 3 (成本级)

    在路由中用作 Depends:
        @router.post("/xxx")
        async def handler(user: dict = Depends(rate_limit_dep)):
            ...
    """
    # 从请求中获取 user_id（中间件已注入）
    user_id = getattr(request.state, "user_id", None) or "anonymous"

    # 获取 Redis 连接（可选）
    redis = getattr(request.app.state, "redis", None)

    # Layer 2: 用户级
    user_limiter = UserRateLimiter(redis)
    await user_limiter.check(user_id, max_per_hour=20)

    # Layer 3: 成本级 — 估算 Token（query 长度 * 2 + 2000 基础消耗）
    content_length = int(request.headers.get("content-length", 0))
    estimated_tokens = min(content_length * 2 + 2000, 10000)
    token_limiter = TokenRateLimiter(redis, daily_quota=100000)
    await token_limiter.check_and_deduct(user_id, estimated_tokens)

    return {"user_id": user_id}


# ============================================================
# 限流配置说明
# ============================================================

RATE_LIMIT_CONFIG = {
    "layer_1_ip": {
        "enabled": True,
        "limit": "10/minute",
        "desc": "IP 级限流，防 DDoS",
    },
    "layer_2_user": {
        "enabled": True,
        "limit": "20/hour",
        "desc": "用户级限流，基于 JWT user_id",
    },
    "layer_3_token": {
        "enabled": True,
        "limit": "100000/day",
        "desc": "成本级限流，按 Token 配额",
    },
    "layer_4_concurrency": {
        "enabled": True,
        "limit": 20,
        "desc": "全局并发信号量，防雪崩",
    },
}


def print_rate_limit_config():
    """打印限流配置"""
    print(f"\n{'='*60}")
    print("  4 层限流配置")
    print(f"{'='*60}")
    for layer, config in RATE_LIMIT_CONFIG.items():
        print(f"  {layer}: {config['limit']} - {config['desc']}")
    print(f"{'='*60}")
