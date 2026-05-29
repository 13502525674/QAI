"""
长期记忆管理器 — 基于 pgvector 的研究历史持久化

两张核心表:
  - rag_research_history: 每条研究记录 (id, text, embedding, metadata)
  - rag_user_profile:    用户知识画像 (id, text, embedding, metadata)

用途:
  1. 每次 Workflow 完成后自动记录研究结果
  2. 新研究启动时检索相关历史作为上下文
  3. 用户知识画像随时间自动更新
"""
from typing import List, Dict, Optional, Any
import json
import logging
from datetime import datetime

import psycopg2
from psycopg2.extras import execute_values
from pgvector.psycopg2 import register_vector
from openai import OpenAI

from config.settings import get_settings

logger = logging.getLogger(__name__)


class LongTermMemory:
    """长期记忆 — 基于 pgvector 的研究历史 + 用户画像 (PG 优先，内存降级)"""

    _instance = None

    def __init__(self):
        s = get_settings()
        self._conn_string = (
            f"host={s.pg_host} port={s.pg_port} dbname={s.pg_database} "
            f"user={s.pg_user} password={s.pg_password}"
        )
        self._embedding_client: Optional[OpenAI] = None
        self._available: Optional[bool] = None
        
        # ── 内存降级存储 ──
        self._history: List[Dict] = []
        self._profiles: Dict[str, Dict] = {}
        
        self._init_embedding()

    @property
    def available(self) -> bool:
        """检查 pgvector 是否可用"""
        if self._available is None:
            try:
                conn = psycopg2.connect(self._conn_string, connect_timeout=3)
                register_vector(conn)
                conn.close()
                self._available = True
                logger.info("[Memory] pgvector 已连接")
            except Exception:
                self._available = False
                logger.warning("[Memory] pgvector 不可用，使用内存降级存储")
        return self._available

    def _get_conn(self):
        conn = psycopg2.connect(self._conn_string)
        register_vector(conn)
        return conn

    def _init_embedding(self):
        try:
            s = get_settings()
            self._embedding_client = OpenAI(
                api_key=s.dashscope_api_key,
                base_url=s.dashscope_base_url,
            )
            logger.info("[Memory] Embedding 客户端已就绪")
        except Exception as e:
            logger.warning("[Memory] Embedding 初始化失败: %s", str(e)[:100])

    def _embed(self, text: str) -> Optional[List[float]]:
        """生成单条文本的向量"""
        if not self._embedding_client:
            return None
        try:
            resp = self._embedding_client.embeddings.create(
                model="text-embedding-v3",
                input=[text[:8000]],
            )
            return resp.data[0].embedding
        except Exception as e:
            logger.warning("[Memory] Embedding 失败: %s", str(e)[:100])
            return None

    # ── 研究历史 ──

    def save_research(self, user_id: str, research_id: str,
                      scene_type: str, query: str,
                      output: str, metadata: Optional[Dict] = None) -> bool:
        """保存一次研究成果到长期记忆"""
        record = {
            "research_id": research_id,
            "user_id": user_id,
            "scene_type": scene_type,
            "query": query,
            "output": output[:3000],
            "timestamp": datetime.now().isoformat(),
            **(metadata or {}),
        }

        # ── 内存降级 ──
        if not self.available:
            self._history.insert(0, record)
            logger.info("[Memory(内存)] 已保存研究记录: %s", research_id)
            return True

        text_for_embed = f"[{scene_type}] {query}: {output[:1000]}"
        embedding = self._embed(text_for_embed)

        conn = self._get_conn()
        cur = conn.cursor()
        row = (research_id, text_for_embed,
               embedding,
               json.dumps(record, ensure_ascii=False))
        cur.execute(
            "INSERT INTO rag_research_history (id, text, embedding, metadata) "
            "VALUES (%s, %s, %s::vector, %s::jsonb) "
            "ON CONFLICT (id) DO UPDATE SET text=EXCLUDED.text, "
            "embedding=EXCLUDED.embedding, metadata=EXCLUDED.metadata",
            row,
        )
        conn.commit()
        cur.close()
        conn.close()
        logger.info("[Memory] 已保存研究记录: %s (%s)", research_id, scene_type)
        return True

    def search_history(self, query: str, user_id: str = None,
                       top_k: int = 5) -> List[Dict]:
        """语义检索历史研究记录"""
        # ── 内存降级: 关键词匹配 ──
        if not self.available:
            keywords = query.lower().split()
            scored = []
            for r in self._history:
                text = f"{r.get('query', '')} {r.get('scene_type', '')}".lower()
                score = sum(1 for kw in keywords if kw in text) / max(len(keywords), 1)
                if score > 0:
                    scored.append((score, {
                        "research_id": r.get("research_id", ""),
                        "text": r.get("query", "")[:200],
                        "score": score,
                        "scene_type": r.get("scene_type", ""),
                        "query": r.get("query", "")[:100],
                        "timestamp": r.get("timestamp", ""),
                    }))
            scored.sort(key=lambda x: x[0], reverse=True)
            return [s[1] for s in scored[:top_k]]

        embedding = self._embed(query)
        if not embedding:
            return self._keyword_search_history(query, top_k)

        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(
            "SELECT id, text, metadata, 1 - (embedding <=> %s::vector) AS score "
            "FROM rag_research_history WHERE embedding IS NOT NULL "
            "ORDER BY embedding <=> %s::vector LIMIT %s",
            (embedding, embedding, top_k),
        )
        rows = cur.fetchall()
        cur.close()
        conn.close()

        results = []
        for r in rows:
            meta = r[2] if isinstance(r[2], dict) else json.loads(r[2])
            results.append({
                "research_id": r[0],
                "text": r[1][:200],
                "score": float(r[3]),
                "scene_type": meta.get("scene_type", ""),
                "query": meta.get("query", "")[:100],
                "timestamp": meta.get("timestamp", ""),
                "metadata": meta,
            })
        return results

    def _keyword_search_history(self, query: str, top_k: int = 5) -> List[Dict]:
        """关键词降级搜索"""
        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(
            "SELECT id, text, metadata FROM rag_research_history ORDER BY created_at DESC LIMIT %s",
            (top_k * 3,),
        )
        rows = cur.fetchall()
        cur.close()
        conn.close()

        keywords = query.lower().split()
        scored = []
        for r in rows:
            text_lower = r[1].lower()
            score = sum(1 for kw in keywords if kw in text_lower) / max(len(keywords), 1)
            if score > 0:
                meta = r[2] if isinstance(r[2], dict) else json.loads(r[2])
                scored.append((score, {
                    "research_id": r[0],
                    "text": r[1][:200],
                    "score": score,
                    "scene_type": meta.get("scene_type", ""),
                    "query": meta.get("query", "")[:100],
                    "timestamp": meta.get("timestamp", ""),
                }))

        scored.sort(key=lambda x: x[0], reverse=True)
        return [s[1] for s in scored[:top_k]]

    def get_recent_history(self, user_id: str = None, limit: int = 10) -> List[Dict]:
        """获取最近研究记录（按时间排序）"""
        # ── 内存降级 ──
        if not self.available:
            items = self._history[:limit]
            return [{
                "research_id": r.get("research_id", ""),
                "scene_type": r.get("scene_type", ""),
                "query": r.get("query", "")[:150],
                "timestamp": r.get("timestamp", ""),
            } for r in items]

        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(
            "SELECT id, text, metadata FROM rag_research_history "
            "ORDER BY created_at DESC LIMIT %s",
            (limit,),
        )
        rows = cur.fetchall()
        cur.close()
        conn.close()

        results = []
        for r in rows:
            meta = r[2] if isinstance(r[2], dict) else json.loads(r[2])
            results.append({
                "research_id": r[0],
                "scene_type": meta.get("scene_type", ""),
                "query": meta.get("query", "")[:150],
                "timestamp": meta.get("timestamp", ""),
            })
        return results

    def get_history_count(self) -> int:
        # ── 内存降级 ──
        if not self.available:
            return len(self._history)

        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute("SELECT COUNT(*) FROM rag_research_history")
        cnt = cur.fetchone()[0]
        cur.close()
        conn.close()
        return cnt

    # ── 用户知识画像 ──

    def update_profile(self, user_id: str, topics: List[str],
                       proficiency: Optional[Dict[str, float]] = None) -> bool:
        """更新用户知识画像"""
        # ── 内存降级 ──
        if not self.available:
            existing = self._profiles.get(user_id, {})
            self._profiles[user_id] = {
                "user_id": user_id,
                "topics": list(set(existing.get("topics", []) + topics)),
                "proficiency": {**existing.get("proficiency", {}), **(proficiency or {})},
                "updated_at": datetime.now().isoformat(),
            }
            logger.info("[Memory(内存)] 已更新用户画像: %s", user_id)
            return True

        profile_text = f"用户 {user_id} 的研究主题: {', '.join(topics)}"
        if proficiency:
            profile_text += f" | 熟练度: {json.dumps(proficiency, ensure_ascii=False)}"

        embedding = self._embed(profile_text)

        conn = self._get_conn()
        cur = conn.cursor()
        meta = {
            "user_id": user_id,
            "topics": topics,
            "proficiency": proficiency or {},
            "updated_at": datetime.now().isoformat(),
        }
        cur.execute(
            "INSERT INTO rag_user_profile (id, text, embedding, metadata) "
            "VALUES (%s, %s, %s::vector, %s::jsonb) "
            "ON CONFLICT (id) DO UPDATE SET text=EXCLUDED.text, "
            "embedding=EXCLUDED.embedding, metadata=EXCLUDED.metadata",
            (user_id, profile_text, embedding, json.dumps(meta, ensure_ascii=False)),
        )
        conn.commit()
        cur.close()
        conn.close()
        logger.info("[Memory] 已更新用户画像: %s (主题数=%d)", user_id, len(topics))
        return True

    def get_profile(self, user_id: str) -> Optional[Dict]:
        """获取用户知识画像"""
        # ── 内存降级 ──
        if not self.available:
            return self._profiles.get(user_id)

        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(
            "SELECT text, metadata FROM rag_user_profile WHERE id = %s",
            (user_id,),
        )
        row = cur.fetchone()
        cur.close()
        conn.close()

        if row:
            meta = row[1] if isinstance(row[1], dict) else json.loads(row[1])
            return meta
        return None

    def find_similar_profiles(self, user_id: str, top_k: int = 3) -> List[Dict]:
        """查找知识画像相似的用户（用于协作推荐）"""
        # ── 内存降级 ──
        if not self.available:
            my_profile = self._profiles.get(user_id, {})
            if not my_profile:
                return []
            my_topics = set(my_profile.get("topics", []))
            results = []
            for uid, profile in self._profiles.items():
                if uid == user_id:
                    continue
                their_topics = set(profile.get("topics", []))
                overlap = len(my_topics & their_topics)
                if overlap > 0:
                    results.append({
                        "user_id": uid,
                        "overlap": overlap,
                        "topics": list(their_topics),
                    })
            results.sort(key=lambda x: x["overlap"], reverse=True)
            return results[:top_k]


# 全局单例
_memory: Optional[LongTermMemory] = None


def get_long_term_memory() -> LongTermMemory:
    global _memory
    if _memory is None:
        _memory = LongTermMemory()
    return _memory