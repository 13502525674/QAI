"""
Experimental Physicist — ReAct Agent (实验物理学家)
Phase 6: 通过 _stream_react 基类方法实现思考流式输出
"""
from typing import Optional, List, Dict, Any

from agents.base import BaseAgent
from tools.registry import get_tool_registry


class ExperimentalPhysicist(BaseAgent):
    """实验物理学家 — 实验方案设计、误差分析、可行性评估"""

    agent_name = "experimental_physicist"
    agent_label = "实验物理学家"

    def _get_temperature(self) -> float:
        return 0.3

    def get_system_prompt(self) -> str:
        return self.load_prompt("experimental_physicist.txt")

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
            f"请按照 ReAct 工作流程：检索已有实验方法 → 设计实验方案 → 误差分析 → 可行性评估 → 结论。"
        )

        return await self._stream_react(
            agent_name=self.agent_name,
            system_prompt=system_prompt,
            tools=tools,
            user_message=user_message,
            recursion_limit=15,
        )


def create_experimental_physicist() -> ExperimentalPhysicist:
    return ExperimentalPhysicist()