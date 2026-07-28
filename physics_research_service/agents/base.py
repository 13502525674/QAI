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
    """所有专业 Agent 的基类 — 支持 mock_mode 节省 token

    P1-8: 三级模型路由 — 子类设置 TASK_TYPE 自动选择模型
    P1-11: ReAct 5 级降级 — 空结果智能处理
    """

    agent_name: str = "base"
    agent_label: str = "基础 Agent"

    # P1-8: 任务类型 — 子类覆盖以启用模型路由
    TASK_TYPE: str = "react_reasoning"

    def __init__(self):
        self.settings = get_settings()
        self._llm: Optional[BaseChatModel] = None

    @property
    def mock_mode(self) -> bool:
        return self.settings.mock_mode

    @property
    def llm(self) -> BaseChatModel:
        """延迟初始化 LLM — P1-8 三级模型路由"""
        if self._llm is None:
            # P1-8: 优先用 ModelRouter 路由
            try:
                from agents.model_router import ModelRouter
                router = ModelRouter.get_instance()
                self._llm = router.get_model(self.TASK_TYPE)
                config = router.get_config(self.TASK_TYPE)
                logger.info(f"[{self.agent_name}] 模型路由: {config['model']} (L{config['level']})")
            except Exception as e:
                # 降级到原逻辑
                logger.warning(f"[{self.agent_name}] 模型路由失败，降级: {e}")
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
        P1-11: 5 级智能降级（连续空结果时 Query 改写 → 换工具 → HyDE → 先验知识 → 终止）
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
        MAX_EMPTY_STREAK = 5       # P1-11: 从 3 提升到 5，配合分级处理

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

                    # P1-11: 5 级智能降级
                    is_empty = self._is_empty_result(obs)
                    if is_empty:
                        empty_streak += 1
                        logger.warning(
                            "[%s] 空结果 streak=%d", agent_name, empty_streak,
                        )

                        # 分级处理
                        if empty_streak == 1:
                            # L1: Query 改写提示
                            await push_agent_thought(
                                agent_name,
                                "[系统提示] 检索未返回结果，请尝试用更短或更宽泛的关键词重新检索"
                            )
                        elif empty_streak == 2:
                            # L2: 换工具提示
                            await push_agent_thought(
                                agent_name,
                                "[系统提示] 仍未检索到结果，请尝试使用 cross_domain_search 工具从其他角度检索"
                            )
                        elif empty_streak == 3:
                            # L3: HyDE 改写
                            try:
                                rewritten = await self._hyde_rewrite(user_message)
                                await push_agent_thought(
                                    agent_name,
                                    f"[系统提示] 请尝试用以下改写后的查询检索: {rewritten}"
                                )
                            except Exception:
                                pass
                        elif empty_streak == 4:
                            # L4: 用先验知识
                            await push_agent_thought(
                                agent_name,
                                "[系统提示] 多次检索未果，请基于你的物理学知识直接作答，并在答案开头标注 [基于先验知识，未经文献验证]"
                            )
                        elif empty_streak >= MAX_EMPTY_STREAK:
                            # L5: 终止
                            logger.warning(
                                "[%s] 连续 %d 次空结果，强制终止 ReAct 循环",
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

    def _is_empty_result(self, obs: str) -> bool:
        """判断是否为空结果"""
        return (
            "(未检索到" in obs
            or "未找到" in obs
            or "no results" in obs.lower()
            or "no relevant" in obs.lower()
            or len(obs.strip()) < 20
        )

    async def _hyde_rewrite(self, original_query: str) -> str:
        """HyDE — 生成假设性文档提取检索关键词"""
        try:
            # 提取原始 query 中的关键词
            prompt = f"""请从以下研究需求中提取 3 个最关键的检索关键词，用英文逗号分隔。
只返回关键词，不要其他内容。

研究需求: {original_query[:200]}
"""
            response = await self.llm.ainvoke([("user", prompt)])
            return response.content.strip()
        except Exception:
            return original_query[:50]

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