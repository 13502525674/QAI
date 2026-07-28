"""
RAG 评估体系 — P0-1 优化
量化 RAG 检索效果：Recall@K / MRR / nDCG / Faithfulness / Context Precision

使用方法:
    python -m rag.eval.evaluator --baseline      # 跑 baseline
    python -m rag.eval.evaluator --reranker      # 跑 reranker 后
    python -m rag.eval.evaluator --compare       # 对比 baseline vs reranker
"""
import re
import json
import time
import logging
import argparse
from typing import List, Dict, Optional
from pathlib import Path

logger = logging.getLogger(__name__)

# ============================================================
# 测试集 — 50 题物理知识 query，覆盖 10 个主题
# 每题标注正确文档的 topic（对应 SEED_DOCUMENTS 的 metadata.topic）
# ============================================================
TEST_SET = [
    # 量子力学 (5 题)
    {"query": "薛定谔方程的形式是什么", "ground_truth_topics": ["quantum_mechanics"], "relevance": 3},
    {"query": "不确定性原理的内容", "ground_truth_topics": ["quantum_mechanics"], "relevance": 3},
    {"query": "波函数的物理意义", "ground_truth_topics": ["quantum_mechanics"], "relevance": 2},
    {"query": "量子叠加态和量子纠缠", "ground_truth_topics": ["quantum_mechanics"], "relevance": 3},
    {"query": "量子力学研究什么", "ground_truth_topics": ["quantum_mechanics"], "relevance": 3},

    # 广义相对论 (5 题)
    {"query": "广义相对论如何解释引力", "ground_truth_topics": ["general_relativity"], "relevance": 3},
    {"query": "爱因斯坦场方程", "ground_truth_topics": ["general_relativity"], "relevance": 3},
    {"query": "引力波是怎么探测的", "ground_truth_topics": ["general_relativity"], "relevance": 2},
    {"query": "黑洞是什么", "ground_truth_topics": ["general_relativity"], "relevance": 2},
    {"query": "等效原理", "ground_truth_topics": ["general_relativity"], "relevance": 2},

    # 标准模型 (5 题)
    {"query": "基本粒子有哪些", "ground_truth_topics": ["standard_model"], "relevance": 3},
    {"query": "希格斯玻色子的发现", "ground_truth_topics": ["standard_model"], "relevance": 3},
    {"query": "夸克有几种", "ground_truth_topics": ["standard_model"], "relevance": 3},
    {"query": "规范玻色子是什么", "ground_truth_topics": ["standard_model"], "relevance": 2},
    {"query": "标准模型规范对称性", "ground_truth_topics": ["standard_model"], "relevance": 2},

    # 弦理论 (5 题)
    {"query": "弦理论的核心思想", "ground_truth_topics": ["string_theory"], "relevance": 3},
    {"query": "M理论需要多少维时空", "ground_truth_topics": ["string_theory"], "relevance": 2},
    {"query": "弦理论的实验验证困难", "ground_truth_topics": ["string_theory"], "relevance": 2},
    {"query": "超弦理论", "ground_truth_topics": ["string_theory"], "relevance": 2},
    {"query": "弦理论统一了什么", "ground_truth_topics": ["string_theory"], "relevance": 3},

    # 凝聚态物理 (5 题)
    {"query": "超导性的BCS理论", "ground_truth_topics": ["condensed_matter"], "relevance": 3},
    {"query": "拓扑绝缘体", "ground_truth_topics": ["condensed_matter"], "relevance": 2},
    {"query": "量子霍尔效应", "ground_truth_topics": ["condensed_matter"], "relevance": 2},
    {"query": "能带理论", "ground_truth_topics": ["condensed_matter"], "relevance": 2},
    {"query": "石墨烯是凝聚态物理吗", "ground_truth_topics": ["condensed_matter"], "relevance": 2},

    # 量子场论 (5 题)
    {"query": "费曼图的作用", "ground_truth_topics": ["quantum_field_theory"], "relevance": 2},
    {"query": "路径积分形式", "ground_truth_topics": ["quantum_field_theory"], "relevance": 2},
    {"query": "重整化是什么", "ground_truth_topics": ["quantum_field_theory"], "relevance": 2},
    {"query": "QED是最精确的物理理论", "ground_truth_topics": ["quantum_field_theory"], "relevance": 3},
    {"query": "场的量子化", "ground_truth_topics": ["quantum_field_theory"], "relevance": 2},

    # 宇宙学 (5 题)
    {"query": "暗能量占宇宙多少比例", "ground_truth_topics": ["cosmology"], "relevance": 3},
    {"query": "宇宙微波背景辐射", "ground_truth_topics": ["cosmology"], "relevance": 3},
    {"query": "暗物质是什么", "ground_truth_topics": ["cosmology"], "relevance": 2},
    {"query": "ΛCDM模型", "ground_truth_topics": ["cosmology"], "relevance": 3},
    {"query": "宇宙演化", "ground_truth_topics": ["cosmology"], "relevance": 2},

    # 量子计算 (5 题)
    {"query": "Shor算法解决什么问题", "ground_truth_topics": ["quantum_computing"], "relevance": 3},
    {"query": "量子比特和经典比特的区别", "ground_truth_topics": ["quantum_computing"], "relevance": 2},
    {"query": "Grover搜索算法", "ground_truth_topics": ["quantum_computing"], "relevance": 2},
    {"query": "超导量子比特实现", "ground_truth_topics": ["quantum_computing"], "relevance": 2},
    {"query": "离子阱量子计算", "ground_truth_topics": ["quantum_computing"], "relevance": 2},

    # AdS/CFT (5 题)
    {"query": "AdS/CFT对偶的含义", "ground_truth_topics": ["ads_cft"], "relevance": 3},
    {"query": "Maldacena猜想", "ground_truth_topics": ["ads_cft"], "relevance": 3},
    {"query": "黑洞信息悖论和AdS/CFT", "ground_truth_topics": ["ads_cft"], "relevance": 2},
    {"query": "引力理论和共形场论的对偶", "ground_truth_topics": ["ads_cft"], "relevance": 2},
    {"query": "量子引力和AdS/CFT", "ground_truth_topics": ["ads_cft"], "relevance": 2},

    # 中微子 (5 题)
    {"query": "中微子振荡表明什么", "ground_truth_topics": ["neutrino_physics"], "relevance": 3},
    {"query": "中微子有几种味道", "ground_truth_topics": ["neutrino_physics"], "relevance": 3},
    {"query": "Super-Kamiokande实验", "ground_truth_topics": ["neutrino_physics"], "relevance": 2},
    {"query": "中微子质量", "ground_truth_topics": ["neutrino_physics"], "relevance": 2},
    {"query": "大亚湾中微子实验", "ground_truth_topics": ["neutrino_physics"], "relevance": 2},
]


class RAGEvaluator:
    """RAG 检索效果评估器"""

    def __init__(self, use_reranker: bool = False):
        """
        Args:
            use_reranker: 是否启用 Reranker（对比测试用）
        """
        from rag.retriever import get_rag_retriever
        self.retriever = get_rag_retriever()
        self.use_reranker = use_reranker
        self.test_set = TEST_SET

    def _retrieve_with_topics(self, query: str, top_k: int = 5) -> List[Dict]:
        """检索并解析返回的文档 topic

        Returns:
            [{"text": "...", "topic": "quantum_mechanics", "score": 0.85}, ...]
        """
        # 调用 retriever
        if self.use_reranker:
            results = self.retriever.retrieve(query, top_k=top_k, use_reranker=True)
        else:
            results = self.retriever.retrieve(query, top_k=top_k)

        parsed = []
        for r in results:
            # 解析格式: "[文献 doc_0_2768321] (向量相关度: 0.85)\n文本..."
            topic = self._extract_topic(r)
            score = self._extract_score(r)
            text = self._extract_text(r)
            parsed.append({"text": text, "topic": topic, "score": score})
        return parsed

    def _extract_topic(self, result_str: str) -> Optional[str]:
        """从检索结果中提取文档 topic

        种子文档的 doc_id 格式: doc_{index}_{hash}
        通过 index 映射到 topic
        """
        # SEED_DOCUMENTS 的顺序对应 topic
        topics = [
            "quantum_mechanics", "general_relativity", "standard_model",
            "string_theory", "condensed_matter", "quantum_field_theory",
            "cosmology", "quantum_computing", "ads_cft", "neutrino_physics",
        ]
        m = re.search(r'\[文献\s*doc_(\d+)_', result_str)
        if m:
            idx = int(m.group(1))
            if 0 <= idx < len(topics):
                return topics[idx]
        # 如果是关键词检索格式: "[文献 1] (关键词匹配: 0.50)"
        # 无法直接映射 topic，用文本匹配
        text = self._extract_text(result_str)
        for i, t in enumerate(topics):
            # 简单关键词匹配
            keywords_map = {
                "quantum_mechanics": ["量子力学", "薛定谔", "波函数", "不确定性", "叠加态", "纠缠"],
                "general_relativity": ["广义相对论", "引力", "时空弯曲", "引力波", "黑洞", "爱因斯坦"],
                "standard_model": ["标准模型", "基本粒子", "夸克", "轻子", "玻色子", "希格斯", "规范"],
                "string_theory": ["弦理论", "弦", "M理论", "超弦", "10维", "11维"],
                "condensed_matter": ["凝聚态", "能带", "超导", "BCS", "拓扑", "霍尔效应", "石墨烯"],
                "quantum_field_theory": ["量子场论", "QFT", "费曼图", "重整化", "路径积分", "QED", "量子化"],
                "cosmology": ["宇宙学", "暗能量", "暗物质", "微波背景", "CMB", "ΛCDM", "宇宙演化"],
                "quantum_computing": ["量子计算", "量子比特", "qubit", "Shor", "Grover", "超导电路", "离子阱"],
                "ads_cft": ["AdS/CFT", "Maldacena", "对偶", "共形场论", "量子引力", "黑洞信息悖论"],
                "neutrino_physics": ["中微子", "中微子振荡", "Super-Kamiokande", "大亚湾", "味道"],
            }
            for kw in keywords_map.get(t, []):
                if kw in text:
                    return t
        return None

    def _extract_score(self, result_str: str) -> float:
        """提取相关度分数"""
        m = re.search(r'相关度:\s*([\d.]+)', result_str)
        if m:
            return float(m.group(1))
        m = re.search(r'匹配:\s*([\d.]+)', result_str)
        if m:
            return float(m.group(1))
        return 0.0

    def _extract_text(self, result_str: str) -> str:
        """提取文本内容（去掉前缀行）"""
        lines = result_str.split("\n", 1)
        if len(lines) > 1:
            return lines[1]
        return result_str

    def evaluate_recall_at_k(self, k: int = 5) -> float:
        """Recall@K: Top-K 中是否包含正确答案"""
        hits = 0
        total = len(self.test_set)
        for case in self.test_set:
            try:
                retrieved = self._retrieve_with_topics(case["query"], top_k=k)
                retrieved_topics = [r["topic"] for r in retrieved if r["topic"]]
                if any(rt in case["ground_truth_topics"] for rt in retrieved_topics):
                    hits += 1
            except Exception as e:
                logger.warning(f"Recall@{k} 评估失败 query='{case['query']}': {e}")
        return hits / total if total > 0 else 0.0

    def evaluate_mrr(self) -> float:
        """MRR: Mean Reciprocal Rank — 第一个正确答案的排名倒数"""
        rr_sum = 0.0
        for case in self.test_set:
            try:
                retrieved = self._retrieve_with_topics(case["query"], top_k=10)
                for i, r in enumerate(retrieved, 1):
                    if r["topic"] and r["topic"] in case["ground_truth_topics"]:
                        rr_sum += 1.0 / i
                        break
            except Exception as e:
                logger.warning(f"MRR 评估失败 query='{case['query']}': {e}")
        return rr_sum / len(self.test_set)

    def evaluate_context_precision(self, k: int = 5) -> float:
        """Context Precision@K: Top-K 中有多少是相关的"""
        precisions = []
        for case in self.test_set:
            try:
                retrieved = self._retrieve_with_topics(case["query"], top_k=k)
                relevant = sum(
                    1 for r in retrieved
                    if r["topic"] and r["topic"] in case["ground_truth_topics"]
                )
                precisions.append(relevant / k)
            except Exception as e:
                logger.warning(f"Context Precision 评估失败: {e}")
                precisions.append(0.0)
        return sum(precisions) / len(precisions) if precisions else 0.0

    def evaluate_ndcg(self, k: int = 5) -> float:
        """nDCG@K: 考虑相关性分数的排序质量"""
        import math
        dcg_sum = 0.0
        count = 0
        for case in self.test_set:
            try:
                retrieved = self._retrieve_with_topics(case["query"], top_k=k)
                # 构建 relevance 列表
                rels = []
                for r in retrieved:
                    if r["topic"] and r["topic"] in case["ground_truth_topics"]:
                        rels.append(case["relevance"])
                    else:
                        rels.append(0)

                # DCG
                dcg = sum(rel / math.log2(i + 2) for i, rel in enumerate(rels))

                # IDCG (理想排序)
                ideal_rels = sorted([case["relevance"]] + [0] * (k - 1), reverse=True)
                idcg = sum(rel / math.log2(i + 2) for i, rel in enumerate(ideal_rels))

                if idcg > 0:
                    dcg_sum += dcg / idcg
                count += 1
            except Exception as e:
                logger.warning(f"nDCG 评估失败: {e}")
        return dcg_sum / count if count > 0 else 0.0

    def evaluate_latency(self, runs: int = 10) -> Dict[str, float]:
        """延迟测试"""
        latencies = []
        for case in self.test_set[:runs]:
            try:
                start = time.time()
                self._retrieve_with_topics(case["query"], top_k=5)
                latencies.append((time.time() - start) * 1000)
            except Exception:
                pass
        if not latencies:
            return {"p50": 0, "p99": 0, "avg": 0}
        latencies.sort()
        return {
            "p50": latencies[len(latencies) // 2],
            "p99": latencies[-1],
            "avg": sum(latencies) / len(latencies),
        }

    def run_full_eval(self) -> Dict:
        """完整评估"""
        print(f"\n{'='*60}")
        print(f"  RAG 评估 ({'with Reranker' if self.use_reranker else 'baseline'})")
        print(f"  测试集: {len(self.test_set)} 题")
        print(f"{'='*60}")

        print("\n[1/5] 评估 Recall@5...")
        recall_5 = self.evaluate_recall_at_k(5)

        print("[2/5] 评估 Recall@10...")
        recall_10 = self.evaluate_recall_at_k(10)

        print("[3/5] 评估 MRR...")
        mrr = self.evaluate_mrr()

        print("[4/5] 评估 Context Precision@5...")
        precision = self.evaluate_context_precision(5)

        print("[5/5] 评估 nDCG@5 和 延迟...")
        ndcg = self.evaluate_ndcg(5)
        latency = self.evaluate_latency()

        result = {
            "mode": "reranker" if self.use_reranker else "baseline",
            "test_set_size": len(self.test_set),
            "recall@5": round(recall_5, 4),
            "recall@10": round(recall_10, 4),
            "mrr": round(mrr, 4),
            "context_precision@5": round(precision, 4),
            "ndcg@5": round(ndcg, 4),
            "latency_p50_ms": round(latency["p50"], 1),
            "latency_p99_ms": round(latency["p99"], 1),
            "latency_avg_ms": round(latency["avg"], 1),
        }

        print(f"\n{'─'*40}")
        print(f"  结果汇总")
        print(f"{'─'*40}")
        for k, v in result.items():
            print(f"  {k}: {v}")

        return result


def save_result(result: Dict, filename: str = None):
    """保存评估结果"""
    results_dir = Path(__file__).parent / "results"
    results_dir.mkdir(exist_ok=True)
    if filename is None:
        filename = f"eval_{result['mode']}_{int(time.time())}.json"
    filepath = results_dir / filename
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(result, f, indent=2, ensure_ascii=False)
    print(f"\n结果已保存到: {filepath}")


def compare_results():
    """对比 baseline vs reranker"""
    results_dir = Path(__file__).parent / "results"
    files = sorted(results_dir.glob("eval_*.json"), key=lambda p: p.stat().st_mtime)

    if len(files) < 2:
        print("需要至少 2 个评估结果文件才能对比")
        return

    results = []
    for f in files[-2:]:  # 取最近 2 个
        with open(f, encoding="utf-8") as fh:
            results.append(json.load(fh))

    print(f"\n{'='*60}")
    print(f"  对比结果")
    print(f"{'='*60}")
    print(f"{'指标':<25} {'Baseline':<15} {'Reranker':<15} {'提升':<15}")
    print(f"{'─'*70}")

    metrics = ["recall@5", "recall@10", "mrr", "context_precision@5", "ndcg@5",
               "latency_p50_ms", "latency_p99_ms"]
    for m in metrics:
        v1 = results[0].get(m, 0)
        v2 = results[1].get(m, 0)
        if v1 > 0 and m.startswith(("recall", "mrr", "precision", "ndcg")):
            change = f"+{((v2 - v1) / v1 * 100):.1f}%"
        elif m.startswith("latency"):
            change = f"{v2 - v1:+.1f}ms"
        else:
            change = "-"
        print(f"  {m:<25} {v1:<15} {v2:<15} {change:<15}")


if __name__ == "__main__":
    logging.basicConfig(level=logging.WARNING)
    parser = argparse.ArgumentParser()
    parser.add_argument("--baseline", action="store_true", help="跑 baseline 评估")
    parser.add_argument("--reranker", action="store_true", help="跑 reranker 评估")
    parser.add_argument("--compare", action="store_true", help="对比结果")
    args = parser.parse_args()

    if args.baseline:
        evaluator = RAGEvaluator(use_reranker=False)
        result = evaluator.run_full_eval()
        save_result(result)

    if args.reranker:
        evaluator = RAGEvaluator(use_reranker=True)
        result = evaluator.run_full_eval()
        save_result(result)

    if args.compare:
        compare_results()
