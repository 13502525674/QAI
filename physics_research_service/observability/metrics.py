"""
可观测性模块 — P0-3 LangSmith + Prometheus 指标

提供 LLM 调用监控指标：
- llm_calls_total: LLM 调用次数（按 agent/model/status）
- llm_latency_seconds: LLM 调用延迟
- llm_tokens_total: Token 消耗
- rag_retrieval_seconds: RAG 检索延迟
- workflow_stage_duration_seconds: 工作流阶段耗时
"""
import logging
import time
from contextlib import contextmanager
from typing import Optional

logger = logging.getLogger(__name__)

# 尝试导入 prometheus_client（可选）
try:
    from prometheus_client import Counter, Histogram, Gauge, generate_latest, CONTENT_TYPE_LATEST
    PROMETHEUS_AVAILABLE = True
except ImportError:
    PROMETHEUS_AVAILABLE = False
    logger.info("[metrics] prometheus_client 未安装，仅日志输出")

if PROMETHEUS_AVAILABLE:
    # LLM 调用次数
    LLM_CALLS = Counter(
        "llm_calls_total",
        "Total LLM API calls",
        ["agent_name", "model", "status"],
    )

    # LLM 调用延迟
    LLM_LATENCY = Histogram(
        "llm_latency_seconds",
        "LLM call latency",
        ["agent_name", "model"],
        buckets=[0.5, 1, 2, 5, 10, 30, 60],
    )

    # Token 消耗
    TOKEN_USAGE = Counter(
        "llm_tokens_total",
        "Total tokens consumed",
        ["agent_name", "model", "type"],  # type: prompt/completion
    )

    # RAG 检索延迟
    RAG_LATENCY = Histogram(
        "rag_retrieval_seconds",
        "RAG retrieval latency",
        ["stage"],  # stage: embed/search/rerank
        buckets=[0.05, 0.1, 0.2, 0.5, 1, 2],
    )

    # Workflow 阶段耗时
    WORKFLOW_STAGE_DURATION = Histogram(
        "workflow_stage_duration_seconds",
        "Workflow stage duration",
        ["scene_type", "stage"],
        buckets=[1, 5, 10, 30, 60, 120],
    )

    # 活跃工作流数
    ACTIVE_WORKFLOWS = Gauge(
        "active_workflows",
        "Number of active workflows",
    )
else:
    # 占位符
    LLM_CALLS = None
    LLM_LATENCY = None
    TOKEN_USAGE = None
    RAG_LATENCY = None
    WORKFLOW_STAGE_DURATION = None
    ACTIVE_WORKFLOWS = None


@contextmanager
def track_llm_call(agent_name: str, model: str):
    """跟踪 LLM 调用（上下文管理器）

    用法:
        with track_llm_call("theoretical_physicist", "deepseek-v4-flash"):
            result = await llm.ainvoke(...)
    """
    start = time.time()
    status = "success"
    try:
        yield
    except Exception as e:
        status = "error"
        raise
    finally:
        duration = time.time() - start
        if PROMETHEUS_AVAILABLE:
            LLM_CALLS.labels(agent_name=agent_name, model=model, status=status).inc()
            LLM_LATENCY.labels(agent_name=agent_name, model=model).observe(duration)
        logger.info(
            f"[metrics] llm_call agent={agent_name} model={model} "
            f"status={status} duration={duration:.2f}s"
        )


@contextmanager
def track_rag_stage(stage: str):
    """跟踪 RAG 检索阶段"""
    start = time.time()
    try:
        yield
    finally:
        duration = time.time() - start
        if PROMETHEUS_AVAILABLE:
            RAG_LATENCY.labels(stage=stage).observe(duration)
        logger.info(f"[metrics] rag stage={stage} duration={duration:.3f}s")


@contextmanager
def track_workflow_stage(scene_type: str, stage: str):
    """跟踪工作流阶段"""
    start = time.time()
    try:
        yield
    finally:
        duration = time.time() - start
        if PROMETHEUS_AVAILABLE:
            WORKFLOW_STAGE_DURATION.labels(scene_type=scene_type, stage=stage).observe(duration)
        logger.info(
            f"[metrics] workflow scene={scene_type} stage={stage} duration={duration:.2f}s"
        )


def record_token_usage(agent_name: str, model: str, prompt_tokens: int, completion_tokens: int):
    """记录 Token 消耗"""
    if PROMETHEUS_AVAILABLE:
        TOKEN_USAGE.labels(agent_name=agent_name, model=model, type="prompt").inc(prompt_tokens)
        TOKEN_USAGE.labels(agent_name=agent_name, model=model, type="completion").inc(completion_tokens)
    logger.info(
        f"[metrics] tokens agent={agent_name} model={model} "
        f"prompt={prompt_tokens} completion={completion_tokens}"
    )
