"""
Redis 短期记忆 — 对话上下文/会话摘要

功能:
  - 保存多轮对话摘要 (session summary)
  - 注入历史上下文到新研究请求
  - 自动过期 (TTL 默认 2 小时)
  - Redis 不可用时使用内存降级存储
"""
import json
import time
import threading
from typing import Optional, List, Dict, Any
from dataclasses import dataclass, field


@dataclass
class SessionRecord:
    """会话记录"""
    research_id: str
    user_id: str
    scene_type: str
    query: str
    summary: str           # 摘要 (前 500 字符)
    topics: List[str]      # 提取的关键词
    timestamp: float
    ttl: int = 7200        # 默认 2 小时


class InMemoryStore:
    """内存降级存储 (Redis 不可用时)"""

    def __init__(self):
        self._store: Dict[str, Dict[str, str]] = {}
        self._expiry: Dict[str, float] = {}
        self._lock = threading.Lock()
        self._cleanup_thread = threading.Thread(target=self._cleanup_loop, daemon=True)
        self._cleanup_thread.start()

    def _cleanup_loop(self):
        while True:
            time.sleep(300)  # 每 5 分钟清理
            self._cleanup()

    def _cleanup(self):
        now = time.time()
        with self._lock:
            expired = [k for k, t in self._expiry.items() if t < now]
            for k in expired:
                self._store.pop(k, None)
                self._expiry.pop(k, None)

    def set(self, key: str, value: str, ttl: int = 7200):
        with self._lock:
            self._store[key] = value
            self._expiry[key] = time.time() + ttl

    def get(self, key: str) -> Optional[str]:
        with self._lock:
            expiry = self._expiry.get(key, 0)
            if expiry < time.time():
                self._store.pop(key, None)
                self._expiry.pop(key, None)
                return None
            return self._store.get(key)

    def keys(self, pattern: str = "*") -> list:
        with self._lock:
            prefix = pattern.replace("*", "")
            return [k for k in self._store if k.startswith(prefix)]


class ShortTermMemory:
    """短期记忆管理器 — Redis 优先，内存降级"""

    def __init__(self):
        self._redis = None
        self._fallback = InMemoryStore()
        self._init_redis()

    def _init_redis(self):
        try:
            import redis
            r = redis.Redis(host="localhost", port=6379, db=0, socket_connect_timeout=2)
            r.ping()
            self._redis = r
            print("[ShortTermMemory] Redis 已连接")
        except Exception:
            self._redis = None
            print("[ShortTermMemory] Redis 不可用，使用内存降级存储")

    @property
    def available(self) -> bool:
        return self._redis is not None

    def save_session(self, record: SessionRecord):
        """保存会话摘要"""
        key = f"session:{record.user_id}:{record.research_id}"
        data = json.dumps({
            "user_id": record.user_id,
            "research_id": record.research_id,
            "scene_type": record.scene_type,
            "query": record.query,
            "summary": record.summary[:500],
            "topics": record.topics,
            "timestamp": record.timestamp,
        }, ensure_ascii=False)

        if self._redis:
            self._redis.setex(key, record.ttl, data)
            # 维护用户会话列表
            list_key = f"user_sessions:{record.user_id}"
            self._redis.zadd(list_key, {record.research_id: record.timestamp})
            self._redis.expire(list_key, record.ttl * 2)
        else:
            self._fallback.set(key, data, record.ttl)
            list_key = f"user_sessions:{record.user_id}"
            existing = self._fallback.get(list_key) or "[]"
            ids = json.loads(existing)
            ids.append(record.research_id)
            self._fallback.set(list_key, json.dumps(ids[-20:]), record.ttl * 2)

    def get_context(self, user_id: str, max_sessions: int = 5) -> str:
        """获取用户最近会话上下文 (用于注入新研究)"""
        sessions = self.get_recent(user_id, max_sessions)
        if not sessions:
            return ""

        lines = ["## 历史研究上下文 (最近会话)\n"]
        for s in sessions:
            topics_str = ", ".join(s.topics[:5]) if s.topics else "无"
            lines.append(
                f"- [{s.scene_type}] {s.query[:80]}"
                f" | 主题: {topics_str}"
                f" | 摘要: {s.summary[:100].replace(chr(10), ' ')}"
            )
        return "\n".join(lines)

    def get_recent(self, user_id: str, limit: int = 5) -> List[SessionRecord]:
        """获取用户最近 N 条会话"""
        records = []

        if self._redis:
            list_key = f"user_sessions:{user_id}"
            ids = self._redis.zrevrange(list_key, 0, limit - 1)
            ids = [i.decode() if isinstance(i, bytes) else i for i in ids]

            for rid in ids:
                key = f"session:{user_id}:{rid}"
                raw = self._redis.get(key)
                if raw:
                    d = json.loads(raw if isinstance(raw, str) else raw.decode())
                    records.append(SessionRecord(**{k: d.get(k) for k in SessionRecord.__dataclass_fields__}))
        else:
            list_key = f"user_sessions:{user_id}"
            raw = self._fallback.get(list_key) or "[]"
            ids = json.loads(raw)[-limit:]

            for rid in reversed(ids):
                key = f"session:{user_id}:{rid}"
                raw = self._fallback.get(key)
                if raw:
                    d = json.loads(raw)
                    records.append(SessionRecord(**{k: d.get(k) for k in SessionRecord.__dataclass_fields__}))

        return records

    def delete_session(self, user_id: str, research_id: str):
        """删除指定会话"""
        key = f"session:{user_id}:{research_id}"
        if self._redis:
            self._redis.delete(key)
            self._redis.zrem(f"user_sessions:{user_id}", research_id)
        # 内存模式不实现删除 (自动过期)


# 全局单例
_memory: Optional[ShortTermMemory] = None


def get_short_term_memory() -> ShortTermMemory:
    global _memory
    if _memory is None:
        _memory = ShortTermMemory()
    return _memory


def save_session_to_memory(record: SessionRecord):
    """便捷方法: 保存会话"""
    get_short_term_memory().save_session(record)


def get_user_context(user_id: str, max_sessions: int = 5) -> str:
    """便捷方法: 获取用户上下文"""
    return get_short_term_memory().get_context(user_id, max_sessions)


def extract_topics(text: str, max_topics: int = 5) -> List[str]:
    """从文本中提取关键词 (简单规则版, 零 token)"""
    keywords = [
        "量子", "相对论", "黑洞", "暗物质", "暗能量", "引力波",
        "弦理论", "超对称", "标准模型", "希格斯", "中微子",
        "量子计算", "量子纠缠", "拓扑", "超导", "AdS/CFT",
        "全息", "熵", "信息悖论", "大统一", "圈量子引力",
        "暴涨", "多重宇宙", "人择原理", "EPR", "贝尔不等式",
        "重整化", "规范场", "自发对称性破缺", "有效场论",
    ]
    found = [kw for kw in keywords if kw in text]
    return found[:max_topics] if found else ["物理研究"]