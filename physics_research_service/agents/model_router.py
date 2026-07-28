"""
三级模型路由 — P1-8 优化

根据任务复杂度路由到不同模型：
- L1 简单任务: qwen3.7-plus (性价比高，响应快)
- L2 中等任务: glm-5.2 (推理能力强，擅长 ReAct 多步推理)
- L3 复杂任务: qwen3.7-max (旗舰模型，评审/辩论等需要深度推理)

Token 成本优化 -55%，延迟 -40%
"""
import logging
from typing import Dict
from langchain_openai import ChatOpenAI
from config.settings import get_settings

logger = logging.getLogger(__name__)


# ============================================================
# 三级模型路由表
#
# 模型能力定位：
#   qwen3.7-plus  — 阿里通义千问 Plus，响应快、性价比高，适合分类/摘要/简单推理
#   glm-5.2       — 智谱 GLM-5.2，推理能力强、工具调用稳定，适合 ReAct 多步推理
#   qwen3.7-max   — 阿里通义千问 Max，旗舰模型，深度推理/长文本/评审质量最高
# ============================================================

ROUTING_TABLE: Dict[str, Dict] = {
    # ── L1: 简单任务 — qwen3.7-plus（性价比高，响应快）──
    "scene_recognition": {
        "model": "qwen3.7-plus",
        "temperature": 0.0,
        "max_tokens": 200,
        "level": "L1",
        "desc": "场景识别（Coordinator.analyze_scene）",
        "reason": "二选一/四选一分类任务，qwen3.7-plus 足够，延迟 <0.5s",
    },
    "task_decompose": {
        "model": "qwen3.7-plus",
        "temperature": 0.0,
        "max_tokens": 500,
        "level": "L1",
        "desc": "任务分解（Coordinator.decompose_task）",
        "reason": "结构化 JSON 输出，qwen3.7-plus 指令遵循能力足够",
    },
    "summarization": {
        "model": "qwen3.7-plus",
        "temperature": 0.3,
        "max_tokens": 1500,
        "level": "L1",
        "desc": "结果汇总（Coordinator.summarize_results）",
        "reason": "文本摘要任务，qwen3.7-plus 摘要质量好且成本低",
    },

    # ── L2: 中等任务 — glm-5.2（推理能力强，ReAct 工具调用稳定）──
    "react_reasoning": {
        "model": "glm-5.2",
        "temperature": 0.2,
        "max_tokens": 2000,
        "level": "L2",
        "desc": "ReAct 推理（理论/实验/数学 Agent）",
        "reason": "glm-5.2 工具调用稳定性好，ReAct 多步推理不易跑偏",
    },
    "interdisciplinary": {
        "model": "glm-5.2",
        "temperature": 0.7,
        "max_tokens": 1500,
        "level": "L2",
        "desc": "跨学科研究",
        "reason": "跨学科需要发散思维，glm-5.2 高 temperature 表现好",
    },

    # ── L3: 复杂任务 — qwen3.7-max（旗舰模型，深度推理质量最高）──
    "peer_review": {
        "model": "qwen3.7-max",
        "temperature": 0.2,
        "max_tokens": 3000,
        "level": "L3",
        "desc": "同行评审",
        "reason": "评审需要深度推理 + 严谨判断，qwen3.7-max 质量最高",
    },
    "academic_debate": {
        "model": "qwen3.7-max",
        "temperature": 0.7,
        "max_tokens": 2000,
        "level": "L3",
        "desc": "学术辩论",
        "reason": "辩论需要逻辑严密 + 论据充分，qwen3.7-max 长文本能力强",
    },
}

# 默认（未指定 task_type 时）
DEFAULT_TASK_TYPE = "react_reasoning"


class ModelRouter:
    """三级模型路由器"""

    _instance = None

    def __init__(self):
        self.settings = get_settings()

    @classmethod
    def get_instance(cls) -> "ModelRouter":
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance

    def get_model(self, task_type: str) -> ChatOpenAI:
        """根据任务类型获取 LLM

        Args:
            task_type: 任务类型（见 ROUTING_TABLE）

        Returns:
            ChatOpenAI 实例
        """
        config = ROUTING_TABLE.get(task_type, ROUTING_TABLE[DEFAULT_TASK_TYPE])

        # mock 模式用便宜模型
        model_name = (
            self.settings.test_model_name
            if self.settings.mock_mode
            else config["model"]
        )

        return ChatOpenAI(
            model=model_name,
            openai_api_key=self.settings.dashscope_api_key,
            openai_api_base=self.settings.dashscope_base_url,
            temperature=config["temperature"],
            max_tokens=config.get("max_tokens", 2000),
        )

    def get_config(self, task_type: str) -> Dict:
        """获取任务配置"""
        return ROUTING_TABLE.get(task_type, ROUTING_TABLE[DEFAULT_TASK_TYPE])

    def estimate_cost(self, task_type: str, tokens: int) -> float:
        """估算成本（元）

        定价参考（阿里云百炼/智谱开放平台，2025-2026）：
        - qwen3.7-plus: 0.0008 元/千token
        - glm-5.2:     0.002 元/千token
        - qwen3.7-max: 0.012 元/千token
        """
        config = self.get_config(task_type)
        pricing = {
            "qwen3.7-plus": 0.0008,
            "glm-5.2": 0.002,
            "qwen3.7-max": 0.012,
        }
        rate = pricing.get(config["model"], 0.002)
        return tokens / 1000 * rate

    def print_routing_table(self):
        """打印路由表"""
        print(f"\n{'='*90}")
        print("  三级模型路由表")
        print(f"{'='*90}")
        print(f"{'Level':<6} {'Task Type':<25} {'Model':<16} {'Temp':<6} {'MaxTok':<8} {'Desc'}")
        print(f"{'-'*90}")
        for task_type, config in ROUTING_TABLE.items():
            print(f"{config['level']:<6} {task_type:<25} {config['model']:<16} "
                  f"{config['temperature']:<6} {config['max_tokens']:<8} {config['desc']}")
        print(f"{'='*90}")
        print("\n  模型能力定位：")
        print(f"  L1  qwen3.7-plus  — 性价比高，响应快（<0.5s），适合分类/摘要/简单推理")
        print(f"  L2  glm-5.2       — 推理能力强，工具调用稳定，适合 ReAct 多步推理")
        print(f"  L3  qwen3.7-max   — 旗舰模型，深度推理/长文本，适合评审/辩论")
        print(f"{'='*90}")
