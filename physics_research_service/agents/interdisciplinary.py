"""
Interdisciplinary Researcher — 跨学科研究者 (直接 LLM 模式)
由于 RAG 工具返回空结果时 ReAct 循环会陷入死循环，改用结构化分析模式
Phase 6: 添加流式输出，逐节推送分析进度
"""
from typing import Optional, List, Dict, Any

from agents.base import BaseAgent
from tools.registry import get_tool_registry


class InterdisciplinaryResearcher(BaseAgent):
    """跨学科研究者 — 跨领域知识迁移、方法论类比、创新视角注入"""

    agent_name = "interdisciplinary_researcher"
    agent_label = "跨学科研究者"

    # P1-8: 跨学科研究用中等模型 + 高 temperature
    TASK_TYPE = "interdisciplinary"

    def _get_temperature(self) -> float:
        return 0.7

    def get_system_prompt(self) -> str:
        return self.load_prompt("interdisciplinary_researcher.txt")

    def get_tools(self) -> List:
        return get_tool_registry().get_by_agent(self.agent_name)

    async def execute(self, task: str, context: Optional[Dict[str, Any]] = None) -> str:
        if self.mock_mode:
            return self._mock_response(task)

        system_prompt = self.get_system_prompt()

        context_str = ""
        if context:
            context_str = "\n\n[上下文信息]\n" + "\n".join(
                f"- {k}: {v}" for k, v in context.items()
            )

        analysis_prompt = f"""
研究任务: {task}{context_str}

请按照以下结构化步骤完成跨学科分析：

## 一、领域映射
- 当前物理问题的核心概念是什么？
- 在数学、化学、生物学、计算机科学、工程学等学科中，有哪些相似或类比的概念/方法？

## 二、方法论类比
- 其他学科中是否有类似问题的成熟解决方案？
- 这些方案是否可以映射/迁移到当前物理问题？
- 迁移时需要注意哪些边界条件差异？

## 三、创新视角注入
- 从跨学科角度，传统物理学视角可能遗漏了什么？
- 提出 2-3 个具体的创新研究方向

## 四、相关性筛选与新颖性评估
- 对上述视角进行筛选：哪些真正有研究价值？哪些仅是表面类比？
- 评估每个方向的新颖性（高/中/低）和潜在影响

## 五、结论
- 最值得深入探索的跨学科方向是什么？
- 建议的第一步行动
"""

        # 流式推送分析进度
        await self._push_structured_progress([
            ("领域映射", "正在分析物理核心概念与跨学科映射..."),
            ("方法论类比", "正在寻找其他学科的可迁移方法..."),
            ("创新视角注入", "正在注入跨学科创新视角..."),
            ("相关性筛选", "正在筛选和评估研究价值..."),
            ("结论", "正在整合最终结论..."),
        ])

        messages = [
            ("system", system_prompt),
            ("user", analysis_prompt),
        ]

        response = await self.llm.ainvoke(messages)
        return response.content

    async def _push_structured_progress(self, steps: list):
        """推送结构化分析的各步骤进度"""
        from streaming.event_bus import push_agent_thought, get_active_bus
        if get_active_bus() is None:
            return
        for section, description in steps:
            await push_agent_thought(
                self.agent_name,
                f"【{section}】{description}"
            )


def create_interdisciplinary_researcher() -> InterdisciplinaryResearcher:
    return InterdisciplinaryResearcher()