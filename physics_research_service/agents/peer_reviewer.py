"""
Academic Peer Reviewer — 结构化评估 Agent (同行评审人)
Phase 3: 不使用 ReAct 循环，而是结构化的多维度评审
Phase 6: 添加流式输出，逐维度推送评审进度
"""
from typing import Optional, List, Dict, Any

from agents.base import BaseAgent
from tools.registry import get_tool_registry


class AcademicPeerReviewer(BaseAgent):
    """同行评审人 — 结构化评审，非 ReAct"""

    agent_name = "peer_reviewer"
    agent_label = "同行评审人"

    def _get_temperature(self) -> float:
        return 0.2

    def get_system_prompt(self) -> str:
        return self.load_prompt("peer_reviewer.txt")

    def get_tools(self) -> List:
        return get_tool_registry().get_by_agent(self.agent_name)

    async def execute(self, task: str, context: Optional[Dict[str, Any]] = None) -> str:
        if self.mock_mode:
            return self._mock_response(task)

        system_prompt = self.get_system_prompt()
        tools = self.get_tools()

        context_str = ""
        other_outputs = ""
        if context:
            if "agent_outputs" in context:
                outputs = context["agent_outputs"]
                other_outputs = "\n\n[待评审的 Agent 输出]\n" + "\n---\n".join(
                    f"【{name}】:\n{text[:800]}" for name, text in outputs.items()
                )
            context_str = "\n".join(
                f"- {k}: {v}" for k, v in context.items() if k != "agent_outputs"
            )
            context_str = f"\n\n[上下文信息]\n{context_str}"

        review_prompt = f"""
评审任务: {task}{context_str}{other_outputs}

请严格按照以下结构完成评审报告：

## 一、逻辑一致性检查
- 论证链条是否完整？有无逻辑跳跃？
- 是否存在循环论证或偷换概念？
- 前提假设是否明确陈述？

## 二、方法论严谨性
- 推导和计算是否正确？
- 近似条件是否合理？
- 实验方案是否可复现？

## 三、创新性评估
- 与其他 Agent 的输出相比，哪些观点有新意？
- 是否提出了传统视角遗漏的方向？

## 四、多维度评分（1-10 分）
| 维度 | 评分 | 说明 |
|------|------|------|
| 创新性 | /10 | |
| 严谨性 | /10 | |
| 可复现性 | /10 | |
| 可读性 | /10 | |
| 综合 | /10 | |

## 五、逐条修改建议
- 按优先级排列（高/中/低）
- 每一条给出具体改进方案

## 六、总体评审意见
"""

        await self._push_review_progress()

        messages = [
            ("system", system_prompt),
            ("user", review_prompt),
        ]

        response = await self.llm.ainvoke(messages)
        return response.content

    async def _push_review_progress(self):
        """推送评审维度进度"""
        from streaming.event_bus import push_agent_thought, get_active_bus
        if get_active_bus() is None:
            return
        steps = [
            "逻辑一致性检查 — 检查论证链条完整性和逻辑跳跃",
            "方法论严谨性 — 验证推导正确性和近似条件合理性",
            "创新性评估 — 对比各 Agent 输出的新颖程度",
            "多维度评分 — 创新性 / 严谨性 / 可复现性 / 可读性 / 综合",
            "逐条修改建议 — 按优先级排列改进方案",
            "总体评审意见 — 整合最终评审结论",
        ]
        for step in steps:
            await push_agent_thought(self.agent_name, f"【评审维度】{step}")


def create_peer_reviewer() -> AcademicPeerReviewer:
    return AcademicPeerReviewer()