"""
BGE-Reranker / DashScope gte-rerank 重排序服务 — P0-2 优化

优先使用 DashScope gte-rerank API（云端，无需本地 torch）；
如果本地装了 FlagEmbedding + torch，可切换为本地 BGE-Reranker-v2-m3。
"""
import logging
from typing import List, Tuple, Optional
from config.settings import get_settings

logger = logging.getLogger(__name__)


class RerankerService:
    """重排序服务 — 单例"""

    _instance: Optional["RerankerService"] = None

    def __init__(self):
        self._mode = "none"  # "dashscope" | "local" | "none"
        self._local_model = None
        self._client = None
        self._init_reranker()

    @classmethod
    def get_instance(cls) -> "RerankerService":
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance

    def _init_reranker(self):
        """初始化 reranker — 优先本地 BGE，其次 DashScope API"""
        s = get_settings()

        # 方案 1: 尝试本地 FlagEmbedding
        try:
            from FlagEmbedding import FlagReranker
            self._local_model = FlagReranker("BAAI/bge-reranker-v2-m3", use_fp16=True)
            self._mode = "local"
            logger.info("[Reranker] 本地 BGE-Reranker-v2-m3 已加载")
            return
        except ImportError:
            logger.info("[Reranker] FlagEmbedding 未安装，尝试 DashScope API")
        except Exception as e:
            logger.warning(f"[Reranker] 本地 BGE 加载失败: {e}")

        # 方案 2: DashScope gte-rerank API
        try:
            from openai import OpenAI
            self._client = OpenAI(
                api_key=s.dashscope_api_key,
                base_url=s.dashscope_base_url,
            )
            self._mode = "dashscope"
            logger.info("[Reranker] DashScope gte-rerank API 已就绪")
            return
        except Exception as e:
            logger.warning(f"[Reranker] DashScope API 初始化失败: {e}")

        self._mode = "none"
        logger.warning("[Reranker] 未启用，将降级到向量检索结果")

    @property
    def available(self) -> bool:
        return self._mode != "none"

    def rerank(self, query: str, documents: List[str], top_k: int = 5) -> List[Tuple[str, float]]:
        """重排序文档

        Args:
            query: 查询
            documents: 候选文档列表
            top_k: 返回前 K 个

        Returns:
            [(doc, score), ...] 按分数降序
        """
        if not documents:
            return []

        if self._mode == "none":
            # 降级：直接返回原始顺序
            return [(doc, 0.0) for doc in documents[:top_k]]

        try:
            if self._mode == "local":
                return self._rerank_local(query, documents, top_k)
            elif self._mode == "dashscope":
                return self._rerank_dashscope(query, documents, top_k)
        except Exception as e:
            logger.warning(f"[Reranker] 重排失败，降级到原始顺序: {e}")
            return [(doc, 0.0) for doc in documents[:top_k]]

    def _rerank_local(self, query: str, documents: List[str], top_k: int) -> List[Tuple[str, float]]:
        """本地 BGE-Reranker"""
        pairs = [[query, doc[:512]] for doc in documents]
        scores = self._local_model.compute_score(pairs, normalize=True)
        if isinstance(scores, float):
            scores = [scores]
        ranked = sorted(zip(documents, scores), key=lambda x: x[1], reverse=True)
        return ranked[:top_k]

    def _rerank_dashscope(self, query: str, documents: List[str], top_k: int) -> List[Tuple[str, float]]:
        """DashScope gte-rerank API

        注意：DashScope 的 rerank 接口需要用专门的 endpoint
        https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank
        """
        # DashScope rerank 请求
        import json
        import requests

        s = get_settings()
        url = "https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank"
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {s.dashscope_api_key}",
        }
        payload = {
            "model": "gte-rerank",
            "input": {
                "query": query,
                "documents": [doc[:512] for doc in documents],
            },
            "parameters": {
                "top_n": top_k,
                "return_documents": False,
            },
        }

        resp = requests.post(url, headers=headers, json=payload, timeout=10)
        resp.raise_for_status()
        data = resp.json()

        # 解析结果: {"output": {"results": [{"index": 0, "relevance_score": 0.95}, ...]}}
        results = data.get("output", {}).get("results", [])
        ranked = []
        for r in results:
            idx = r["index"]
            score = r.get("relevance_score", 0.0)
            ranked.append((documents[idx], float(score)))

        return ranked[:top_k]
