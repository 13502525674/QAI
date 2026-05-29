"""
Mathematical Methodologist — ReAct Agent (数学方法论专家)
Phase 6: 通过 _stream_react 基类方法实现思考流式输出
"""
from typing import Optional, List, Dict, Any

from agents.base import BaseAgent
from tools.registry import get_tool_registry


class MathematicalMethodologist(BaseAgent):
    """数学方法论专家 — 公式推导、量纲分析、数值模拟、逻辑严格性检查"""

    agent_name = "mathematical_methodologist"
    agent_label = "数学方法论专家"

    def _get_temperature(self) -> float:
        return 0.1

    def get_system_prompt(self) -> str:
        return self.load_prompt("mathematical_methodologist.txt")

    def get_tools(self) -> List:
        return get_tool_registry().get_by_agent(self.agent_name)

    async def execute(self, task: str, context: Optional[Dict[str, Any]] = None) -> str:
        if self.mock_mode:
            return self._mock_response(task)

        system_prompt = self.get_system_prompt()
        tools = self.get_tools()

        context_str = ""
        if context:
            context_str = "\n\n[上下文信息]\n" + "\n".join(
                f"- {k}: {v}" for k, v in context.items()
            )

        user_message = (
            f"研究任务: {task}{context_str}\n\n"
            f"请按照 ReAct 流程：符号计算验证 → 量纲分析 → 近似方法评估 → 逻辑严格性检查 → 结论。"
        )

        return await self._stream_react(
            agent_name=self.agent_name,
            system_prompt=system_prompt,
            tools=tools,
            user_message=user_message,
            recursion_limit=30,
        )


def create_mathematical_methodologist() -> MathematicalMethodologist:
    return MathematicalMethodologist()