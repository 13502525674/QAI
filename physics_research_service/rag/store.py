"""
RAG 层 — pgvector 向量库管理与文档检索
Phase 5 升级: Chroma → pgvector (PostgreSQL)
保留 3 个 Collection → 对应 3 张 pgvector 表
"""
import logging
import psycopg2
from psycopg2.extras import execute_values
from pgvector.psycopg2 import register_vector
from config.settings import get_settings

logger = logging.getLogger(__name__)

# 三个 Collection → 三张 pgvector 表
_TABLES = {
    "knowledge_base":    "rag_knowledge_base",
    "research_history":  "rag_research_history",
    "user_profile":      "rag_user_profile",
}

_CREATE_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS {table} (
    id TEXT PRIMARY KEY,
    text TEXT NOT NULL,
    embedding vector(1024),
    metadata JSONB DEFAULT '{{}}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_{short}_embedding ON {table}
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 200);
"""


class VectorStoreManager:
    """pgvector 向量库管理器 — 管理 3 张表"""

    TABLES = _TABLES  # 兼容旧接口

    def __init__(self):
        s = get_settings()
        self._conn_string = (
            f"host={s.pg_host} port={s.pg_port} dbname={s.pg_database} "
            f"user={s.pg_user} password={s.pg_password}"
        )
        self._ensure_tables()

    def _get_conn(self):
        conn = psycopg2.connect(self._conn_string)
        register_vector(conn)
        return conn

    def _ensure_tables(self):
        """确保 3 张表存在（幂等）"""
        conn = self._get_conn()
        cur = conn.cursor()
        for name, table in _TABLES.items():
            short = name[:20]
            sql = _CREATE_TABLE_SQL.format(table=table, short=short)
            cur.execute(sql)
            logger.info("[pgvector] 数据表 %s 已就绪", table)
        conn.commit()
        cur.close()
        conn.close()

    def get_collection(self, name: str):
        """返回表级别的访问器（兼容旧 API）"""
        if name not in _TABLES:
            raise ValueError(f"Unknown collection: {name}")
        return _PgvectorTableAccessor(self._conn_string, _TABLES[name])


class _PgvectorTableAccessor:
    """单表访问器 — 兼容 Chroma Collection API"""

    def __init__(self, conn_string: str, table: str):
        self._conn_string = conn_string
        self.table = table

    def _get_conn(self):
        conn = psycopg2.connect(self._conn_string)
        register_vector(conn)
        return conn

    def add(self, ids, documents=None, embeddings=None, metadatas=None):
        conn = self._get_conn()
        cur = conn.cursor()
        if documents is None:
            documents = [""] * len(ids)
        if metadatas is None:
            metadatas = [{}] * len(ids)
        if embeddings is None:
            embeddings = [None] * len(ids)

        rows = [(r[0], r[1], r[2], json_dumps(r[3]))
                for r in zip(ids, documents, embeddings, metadatas)]
        execute_values(cur,
            f"INSERT INTO {self.table} (id, text, embedding, metadata) "
            f"VALUES %s ON CONFLICT (id) DO UPDATE SET text=EXCLUDED.text, "
            f"embedding=EXCLUDED.embedding, metadata=EXCLUDED.metadata",
            rows, template="(%s, %s, %s::vector, %s::jsonb)")
        conn.commit()
        cur.close()
        conn.close()

    def query(self, query_embeddings=None, n_results=5, where=None):
        """向量查询"""
        if query_embeddings is None:
            return {"ids": [], "documents": [], "metadatas": [], "distances": []}

        conn = self._get_conn()
        cur = conn.cursor()
        emb = query_embeddings[0] if isinstance(query_embeddings, list) else query_embeddings
        cur.execute(
            f"SELECT id, text, metadata, embedding <=> %s::vector AS distance "
            f"FROM {self.table} WHERE embedding IS NOT NULL "
            f"ORDER BY embedding <=> %s::vector LIMIT %s",
            (emb, emb, n_results),
        )
        rows = cur.fetchall()
        cur.close()
        conn.close()

        return {
            "ids": [[r[0] for r in rows]],
            "documents": [[r[1] for r in rows]],
            "metadatas": [[r[2] for r in rows]],
            "distances": [[float(r[3]) for r in rows]],
        }

    def count(self):
        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(f"SELECT COUNT(*) FROM {self.table}")
        cnt = cur.fetchone()[0]
        cur.close()
        conn.close()
        return cnt

    def get(self, ids=None, include=None):
        """按 ID 获取文档"""
        conn = self._get_conn()
        cur = conn.cursor()
        if ids:
            cur.execute(
                f"SELECT id, text, metadata FROM {self.table} WHERE id = ANY(%s)",
                (list(ids),),
            )
        else:
            cur.execute(
                f"SELECT id, text, metadata FROM {self.table}"
            )
        rows = cur.fetchall()
        cur.close()
        conn.close()
        return {
            "ids": [r[0] for r in rows],
            "documents": [r[1] for r in rows],
            "metadatas": [r[2] for r in rows],
        }


def json_dumps(obj):
    import json
    return json.dumps(obj, ensure_ascii=False)


# 全局单例
_vector_store: VectorStoreManager = None


def get_vector_store() -> VectorStoreManager:
    global _vector_store
    if _vector_store is None:
        _vector_store = VectorStoreManager()
    return _vector_store