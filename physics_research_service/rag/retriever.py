"""
RAG 检索器 — LlamaIndex + pgvector (PostgreSQL)
Phase 3 升级: 使用 PostgreSQL pgvector 作为向量数据库，LlamaIndex 作为文档框架。
          psycopg2 + pgvector Python 客户端直连，避免 llama-index-vector-stores 版本冲突。

架构: LlamaIndex (Document) → psycopg2 + pgvector → PostgreSQL 16 + pgvector 0.8.1
"""
from typing import List, Optional
import re
import logging
import numpy as np

import psycopg2
from psycopg2.extras import execute_values
from pgvector.psycopg2 import register_vector
from openai import OpenAI

from config.settings import get_settings

logger = logging.getLogger(__name__)

# pgvector 表结构，兼容 LlamaIndex 语义
_CREATE_TABLE = """
CREATE TABLE IF NOT EXISTS llama_rag_docs (
    id TEXT PRIMARY KEY,
    text TEXT NOT NULL,
    embedding vector(1024),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rag_embedding ON llama_rag_docs
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 200);
"""


class PgvectorStore:
    """轻量级 pgvector 存储 — 封装 psycopg2 + pgvector"""

    def __init__(self, conn_string: str, table_name: str = "llama_rag_docs"):
        self._conn_string = conn_string
        self._table = table_name
        self._init_db()

    def _get_conn(self):
        conn = psycopg2.connect(self._conn_string)
        register_vector(conn)
        return conn

    def _init_db(self):
        """建表 + 注册 pgvector 类型"""
        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(_CREATE_TABLE)
        conn.commit()
        cur.close()
        conn.close()
        logger.info("[pgvector] 数据表 %s 已就绪", self._table)

    def add(self, ids: List[str], texts: List[str],
            embeddings: Optional[List[List[float]]] = None,
            metadatas: Optional[List[dict]] = None) -> int:
        """批量添加文档"""
        import json

        conn = self._get_conn()
        cur = conn.cursor()
        rows = [(doc_id, text, embeddings[i] if embeddings else None,
                 json.dumps(metadatas[i] if metadatas else {}, ensure_ascii=False))
                for i, (doc_id, text) in enumerate(zip(ids, texts))]

        execute_values(cur,
            f"INSERT INTO {self._table} (id, text, embedding, metadata) VALUES %s ON CONFLICT (id) DO NOTHING",
            rows, template="(%s, %s, %s::vector, %s::jsonb)")
        count = cur.rowcount
        conn.commit()
        cur.close()
        conn.close()
        return count

    def search(self, query_embedding: List[float], top_k: int = 5) -> List[tuple]:
        """余弦相似度搜索, 返回 [(id, text, score), ...]"""
        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(
            f"SELECT id, text, 1 - (embedding <=> %s::vector) AS score "
            f"FROM {self._table} "
            f"WHERE embedding IS NOT NULL "
            f"ORDER BY embedding <=> %s::vector "
            f"LIMIT %s",
            (query_embedding, query_embedding, top_k),
        )
        rows = cur.fetchall()
        cur.close()
        conn.close()
        return [(r[0], r[1], float(r[2])) for r in rows]

    def count(self) -> int:
        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(f"SELECT COUNT(*) FROM {self._table}")
        cnt = cur.fetchone()[0]
        cur.close()
        conn.close()
        return cnt

    def get_all_texts(self) -> List[str]:
        """获取所有文档文本"""
        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(f"SELECT text FROM {self._table}")
        rows = cur.fetchall()
        cur.close()
        conn.close()
        return [r[0] for r in rows]

    def clear(self):
        conn = self._get_conn()
        cur = conn.cursor()
        cur.execute(f"DELETE FROM {self._table}")
        conn.commit()
        cur.close()
        conn.close()


# ============================================================
# RAGRetriever — 封装 PgvectorStore + OpenAI Embedding
# ============================================================
class RAGRetriever:
    """物理文献检索器 — pgvector 向量库 + openai embedding (LlamaIndex 框架)"""

    def __init__(self, collection_name: str = "knowledge_base"):
        s = get_settings()
        conn_str = (
            f"host={s.pg_host} port={s.pg_port} dbname={s.pg_database} "
            f"user={s.pg_user} password={s.pg_password}"
        )
        self._store = PgvectorStore(conn_str)
        self._embedding_client: Optional[OpenAI] = None
        self._vector_available = False

        self._init_embedding()

    def _init_embedding(self):
        """初始化 OpenAI 兼容的 Embedding 客户端 (百炼 DashScope)"""
        try:
            s = get_settings()
            self._embedding_client = OpenAI(
                api_key=s.dashscope_api_key,
                base_url=s.dashscope_base_url,
            )
            self._vector_available = True
            logger.info("[RAG] Embedding 客户端已就绪 (model=text-embedding-v3)")
        except Exception as e:
            logger.warning("[RAG] Embedding 客户端初始化失败: %s", str(e)[:100])

    def _embed(self, texts: List[str]) -> List[List[float]]:
        """调用 DashScope text-embedding-v3 生成向量"""
        if not self._embedding_client:
            return []

        try:
            resp = self._embedding_client.embeddings.create(
                model="text-embedding-v3",
                input=[t[:8000] for t in texts],
            )
            return [d.embedding for d in resp.data]
        except Exception as e:
            logger.warning("[RAG] Embedding 生成失败: %s", str(e)[:100])
            return []

    # ── 公开 API ──

    def add_documents(self, documents: List) -> None:
        """添加文档到向量库

        Args:
            documents: List[dict] (含 page_content, metadata) 或 LlamaIndex Document
        """
        ids = []
        texts = []
        metadatas = []
        for i, doc in enumerate(documents):
            if hasattr(doc, 'page_content'):
                text = doc.page_content
                meta = getattr(doc, 'metadata', {})
            elif isinstance(doc, dict):
                text = doc.get('page_content', '')
                meta = doc.get('metadata', {})
            else:
                text = str(doc)
                meta = {}
            ids.append(f"doc_{i}_{hash(text) & 0xFFFFFFF}")
            texts.append(text)
            metadatas.append(meta)

        embeddings = self._embed(texts) if self._vector_available else None
        added = self._store.add(ids=ids, texts=texts, embeddings=embeddings, metadatas=metadatas)

        if embeddings:
            logger.info("[RAG] 已添加 %d 篇文档到 pgvector (含向量)", added)
        else:
            logger.info("[RAG] 已添加 %d 篇文档到 pgvector (无向量/关键词回退)", added)

    def _keyword_score(self, query: str, text: str) -> float:
        query_lower = query.lower()
        text_lower = text.lower()
        keywords = re.findall(r'[\w\u4e00-\u9fff]+', query_lower)
        if not keywords:
            return 0.0
        hits = sum(1 for kw in keywords if kw in text_lower)
        return hits / len(keywords)

    def retrieve(self, query: str, top_k: int = 5) -> List[str]:
        """检索相关文献

        Args:
            query: 检索查询
            top_k: 返回结果数

        Returns:
            文献摘要列表
        """
        # 1. 优先向量检索 (pgvector HNSW index)
        if self._vector_available:
            embeddings = self._embed([query])
            if embeddings:
                results = self._store.search(embeddings[0], top_k)
                if results:
                    formatted = []
                    for doc_id, text, score in results:
                        formatted.append(
                            f"[文献 {doc_id}] (向量相关度: {score:.4f})\n{text[:500]}"
                        )
                    return formatted

        # 2. 降级为关键词搜索
        all_texts = self._store.get_all_texts()
        scored = []
        for text in all_texts:
            s = self._keyword_score(query, text)
            if s > 0:
                scored.append((s, text[:500]))
        scored.sort(key=lambda x: x[0], reverse=True)

        results = []
        for i, (score, text) in enumerate(scored[:top_k]):
            results.append(f"[文献 {i+1}] (关键词匹配: {score:.2f})\n{text}")
        return results or ["(未检索到相关文献)"]

    def query(self, query: str) -> str:
        """直接查询并返回拼接结果"""
        results = self.retrieve(query)
        return "\n\n---\n\n".join(results)

    def count(self) -> int:
        """文档数量"""
        return self._store.count()


# 全局单例
_rag_retriever: Optional[RAGRetriever] = None


def get_rag_retriever() -> RAGRetriever:
    global _rag_retriever
    if _rag_retriever is None:
        _rag_retriever = RAGRetriever()
    return _rag_retriever


# ============================================================
# 种子数据 — 启动时自动写入向量库
# ============================================================
SEED_DOCUMENTS = [
    {"page_content": "量子力学是研究微观粒子运动规律的物理学分支。核心概念：波函数与薛定谔方程 iℏ∂ψ/∂t = Ĥψ；不确定性原理 Δx·Δp ≥ ℏ/2；量子叠加态；量子纠缠", "metadata": {"topic": "quantum_mechanics"}},
    {"page_content": "广义相对论将引力解释为时空弯曲。核心方程：Rμν - (1/2)Rgμν + Λgμν = (8πG/c⁴)Tμν。等效原理、时空弯曲、引力波（2015年LIGO首次探测）、黑洞", "metadata": {"topic": "general_relativity"}},
    {"page_content": "粒子物理标准模型描述基本粒子和三种相互作用。基本粒子：夸克(6种)、轻子(6种)、规范玻色子(光子/W/Z/胶子)、希格斯玻色子(2012年发现)。理论框架：SU(3)_C × SU(2)_L × U(1)_Y 规范对称性", "metadata": {"topic": "standard_model"}},
    {"page_content": "弦理论试图统一量子力学和广义相对论。核心思想：基本粒子是微小弦的振动模式，需要10维(超弦)或11维(M理论)时空。主要挑战：缺乏实验验证、景观问题", "metadata": {"topic": "string_theory"}},
    {"page_content": "凝聚态物理研究物质宏观性质的微观起源。重要概念：能带理论、超导性(BCS理论)、拓扑相(量子霍尔效应、拓扑绝缘体)、量子相变。前沿：高温超导、拓扑量子计算、石墨烯", "metadata": {"topic": "condensed_matter"}},
    {"page_content": "量子场论(QFT)是粒子物理的理论基础。核心概念：场的量子化、费曼图、重整化、规范理论。路径积分形式由费曼提出。QED是最精确的物理理论之一", "metadata": {"topic": "quantum_field_theory"}},
    {"page_content": "宇宙学标准模型(ΛCDM)描述宇宙演化。关键成分：暗能量(Λ，占68%)、暗物质(CDM，27%)、普通物质(5%)。宇宙微波背景辐射(CMB)是重要观测证据", "metadata": {"topic": "cosmology"}},
    {"page_content": "量子计算利用量子叠加和纠缠进行计算。量子比特(qubit)是基本单位。重要算法：Shor算法(因子分解)、Grover算法(搜索)。物理实现：超导电路、离子阱、拓扑量子比特", "metadata": {"topic": "quantum_computing"}},
    {"page_content": "AdS/CFT对偶(Maldacena猜想)是弦理论的重要成果。它建立引力理论(AdS空间)与共形场论(CFT)之间的对偶关系。对理解量子引力、黑洞信息悖论有重要意义", "metadata": {"topic": "ads_cft"}},
    {"page_content": "中微子物理是粒子物理的前沿。中微子振荡表明它们有质量(与标准模型预测不一致)。三种味道：电子中微子、μ子中微子、τ子中微子。实验：Super-Kamiokande、Daya Bay", "metadata": {"topic": "neutrino_physics"}},
]