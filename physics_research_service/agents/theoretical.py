"""
Theoretical Physicist — ReAct Agent (理论物理学家)
使用 LangGraph 的 create_react_agent 实现 Thought→Action→Observation 循环
Phase 6: 通过 _stream_react 基类方法实现思考流式输出
"""
from typing import Optional, List, Dict, Any
from langchain_core.messages import SystemMessage

from agents.base import BaseAgent
from tools.registry import get_tool_registry


class TheoreticalPhysicist(BaseAgent):
    """理论物理学家 — ReAct 推理循环"""

    agent_name = "theoretical_physicist"
    agent_label = "理论物理学家"

    def _get_temperature(self) -> float:
        return 0.2

    def get_system_prompt(self) -> str:
        return self.load_prompt("theoretical_physicist.txt")

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

        user_message = f"研究任务: {task}{context_str}\n\n请按照你的 ReAct 工作流程，使用工具检索和分析，最后给出结论。"

        return await self._stream_react(
            agent_name=self.agent_name,
            system_prompt=system_prompt,
            tools=tools,
            user_message=user_message,
            recursion_limit=15,
        )


def create_theoretical_physicist() -> TheoreticalPhysicist:
    return TheoreticalPhysicist()