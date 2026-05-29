"""
BaseAgent 抽象基类 — Phase 2 统一使用 deepseek-v4-flash 通过百炼 OpenAI 兼容接口
Phase 6: 新增 execute_stream 支持 Agent 思考过程流式输出
"""
from abc import ABC, abstractmethod
from typing import Optional, List, Dict, Any, AsyncGenerator
from pathlib import Path
import re
import logging

from langchain.chat_models.base import BaseChatModel
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent

from config.settings import get_settings

logger = logging.getLogger(__name__)


class BaseAgent(ABC):
    """所有专业 Agent 的基类 — 支持 mock_mode 节省 token"""

    agent_name: str = "base"
    agent_label: str = "基础 Agent"

    def __init__(self):
        self.settings = get_settings()
        self._llm: Optional[BaseChatModel] = None

    @property
    def mock_mode(self) -> bool:
        return self.settings.mock_mode

    @property
    def llm(self) -> BaseChatModel:
        """延迟初始化 LLM — mock_mode 时使用便宜模型"""
        if self._llm is None:
            model = self.settings.test_model_name if self.settings.mock_mode else self.settings.dashscope_model_name
            self._llm = ChatOpenAI(
                model=model,
                openai_api_key=self.settings.dashscope_api_key,
                openai_api_base=self.settings.dashscope_base_url,
                temperature=self._get_temperature(),
            )
        return self._llm

    def _mock_response(self, task: str) -> str:
        """mock 模式: 返回预设模板响应，完全不走 LLM"""
        return (
            f"[MOCK] {self.agent_label} 响应:\n"
            f"任务: {task[:100]}\n"
            f"这是一条 mock 响应，用于测试 Workflow 流程，未调用 LLM API。"
        )

    def _execute_with_fallback(self, task: str, context: dict = None) -> str:
        """统一执行入口: mock_mode 时走 mock，否则走 LLM"""
        if self.mock_mode:
            return self._mock_response(task)
        return self._execute_llm(task, context)

    async def _execute_llm(self, task: str, context: dict = None) -> str:
        """子类应覆盖此方法实现真正的 LLM 调用"""
        return self._mock_response(task)

    def _get_temperature(self) -> float:
        """子类可覆盖以设置不同的 temperature"""
        return 0.2

    def load_prompt(self, prompt_filename: str) -> str:
        """加载 config/prompts/ 下的 System Prompt 模板"""
        prompt_path = self.settings.prompts_dir / prompt_filename
        if not prompt_path.exists():
            raise FileNotFoundError(f"Prompt file not found: {prompt_path}")
        return prompt_path.read_text(encoding="utf-8")

    @abstractmethod
    def get_system_prompt(self) -> str:
        """返回该 Agent 的 System Prompt"""
        ...

    @abstractmethod
    def get_tools(self) -> List[Any]:
        """返回该 Agent 的 Tool 列表"""
        ...

    @abstractmethod
    async def execute(self, task: str, context: Optional[Dict[str, Any]] = None) -> str:
        """执行 Agent 任务"""
        ...

    async def _stream_react(self, agent_name: str, system_prompt: str, tools: list,
                            user_message: str, recursion_limit: int = 25) -> str:
        """ReAct 流式执行基类方法 — 自动推送 Thought/Action/Observation 到事件总线

        Phase 7: recursion_limit 从 15 提升到 25，添加空结果保护避免 ReAct 死循环
        """
        from streaming.event_bus import (
            push_agent_thought, push_agent_action,
            push_agent_observation, push_agent_output,
        )

        agent = create_react_agent(
            model=self.llm,
            tools=tools,
            state_modifier=system_prompt,
        )

        result_messages = []
        empty_streak = 0           # 连续空结果计数器
        MAX_EMPTY_STREAK = 3       # 连续 3 次空结果则强制终止

        async for event in agent.astream(
            {"messages": [("user", user_message)]},
            config={"recursion_limit": recursion_limit},
        ):
            if "agent" in event:
                msg = event["agent"]["messages"][-1]
                if hasattr(msg, "content") and msg.content:
                    # 尝试从 AI 消息中分离 Thought 和 Action
                    content = msg.content
                    result_messages.append(content)

                    thought_part = self._extract_thought(content)
                    if thought_part:
                        await push_agent_thought(agent_name, thought_part)

                    if hasattr(msg, "tool_calls") and msg.tool_calls:
                        for tc in msg.tool_calls:
                            tool_name = tc.get("name", "unknown")
                            tool_args = str(tc.get("args", {}))[:300]
                            await push_agent_action(agent_name, tool_name, tool_args)
                    elif "Action:" in content or "Action Input:" in content:
                        tool_name, tool_args = self._extract_action(content)
                        if tool_name:
                            await push_agent_action(agent_name, tool_name, tool_args)

            if "tools" in event:
                tool_msg = event["tools"]["messages"][-1]
                if hasattr(tool_msg, "content") and tool_msg.content:
                    obs = tool_msg.content[:600]
                    await push_agent_observation(agent_name, obs)

                    # 检测空结果：RAG 返回 "(未检索到...)" 或空响应对同类查询多次重试
                    is_empty = (
                        "(未检索到" in obs
                        or "未找到" in obs
                        or "no results" in obs.lower()
                        or len(obs.strip()) < 20
                    )
                    if is_empty:
                        empty_streak += 1
                        if empty_streak >= MAX_EMPTY_STREAK:
                            # 连续多次空结果 → 注入最终提示让 Agent 基于已有知识作答
                            logger.warning(
                                "[%s] 连续 %d 次工具返回空结果，强制终止 ReAct 循环",
                                agent_name, empty_streak,
                            )
                            break
                    else:
                        empty_streak = 0

        # 推送最终输出预览
        for msg in reversed(result_messages):
            if msg and len(msg) > 20:
                await push_agent_output(agent_name, msg[:300])
                return msg

        return "未能完成分析，请重试。"

    def _extract_thought(self, content: str) -> str:
        """从 LLM 输出中提取 Thought 部分"""
        patterns = [
            r'Thought:\s*(.+?)(?=\n\s*(?:Action|Observation|$)|\Z)',
            r'思考[：:]\s*(.+?)(?=\n\s*(?:行动|操作|观察|$)|\Z)',
            r'分析[：:]\s*(.+?)(?=\n\s*(?:结论|行动|$)|\Z)',
        ]
        for pat in patterns:
            m = re.search(pat, content, re.DOTALL | re.IGNORECASE)
            if m:
                thought = m.group(1).strip()
                if len(thought) > 10:
                    return thought
        # 如果没有显式 Thought 标记，截取前 300 字符作为思考内容
        if len(content) > 20 and 'Action:' not in content and '工具' not in content:
            return content[:300]
        return ""

    def _extract_action(self, content: str) -> tuple:
        """从 LLM 输出中提取 Action 名称和参数"""
        name_match = re.search(r'Action:\s*(.+?)(?:\n|$)', content, re.IGNORECASE)
        args_match = re.search(r'Action Input:\s*(.+?)(?:\n|$)', content, re.IGNORECASE)
        tool_name = name_match.group(1).strip() if name_match else None
        tool_args = args_match.group(1).strip()[:300] if args_match else ""
        return tool_name, tool_args