"""
JWT 认证中间件 — 兼容 Java 端 x_access_token
Java 端使用 HMAC256(userPhone) 签名，Python 端无验证解码提取 userId
"""
import jwt
from typing import Optional
from fastapi import Request, HTTPException


async def jwt_auth_middleware(request: Request) -> Optional[dict]:
    """
    从 x_access_token header 中提取用户信息。
    Java 端已完成认证，Python 端仅提取 userId，不做签名验证。
    返回 {"user_id": str, "username": str} 或 None（白名单路径）。
    """
    token = request.headers.get("x_access_token") or request.query_params.get("x_access_token")

    if not token:
        # 允许部分公开路径无 token 访问
        if request.url.path in ("/health", "/api/research/health"):
            return None
        raise HTTPException(status_code=401, detail="缺少认证令牌")

    try:
        # 无验证解码 — Java 端已做认证，Python 端只提取信息
        payload = jwt.decode(token, options={"verify_signature": False})
        return {
            "user_id": payload.get("userId", ""),
            "username": payload.get("username", payload.get("sub", "")),
        }
    except Exception:
        raise HTTPException(status_code=401, detail="令牌解析失败")