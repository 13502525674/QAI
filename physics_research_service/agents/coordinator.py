"""
Research Coordinator — Plan-and-Execute 编排器 (研究协调员)
Phase 3: 负责场景识别、任务分解、Agent 调度、结果汇总
Phase 6: 添加流式输出，推送分析和汇总进度
"""
from typing import Optional, List, Dict, Any
import json

from langgraph.prebuilt import create_react_agent

from agents.base import BaseAgent


class ResearchCoordinator(BaseAgent):
    """研究协调员 — Plan-and-Execute 编排"""

    agent_name = "coordinator"
    agent_label = "研究协调员"

    # P1-8: 场景识别/任务分解用便宜模型
    TASK_TYPE = "scene_recognition"

    # Agent 能力描述（供协调员参考调度）
    AGENT_CAPABILITIES = {
        "theoretical_physicist": "理论物理推演、假设构建、文献综述的理论部分",
        "experimental_physicist": "实验方案设计、误差分析、可行性评估",
        "mathematical_methodologist": "公式推导、量纲分析、数值模拟、逻辑严格性检查",
        "interdisciplinary_researcher": "跨学科类比、创新视角注入、方法论迁移",
        "peer_reviewer": "交叉验证、逻辑一致性检查、多维度评分与修改建议",
    }

    def _get_temperature(self) -> float:
        return 0.1

    def get_system_prompt(self) -> str:
        base_prompt = self.load_prompt("coordinator.txt")
        # 附加 Agent 能力表
        capabilities_str = "\n".join(
            f"  - **{name}**: {desc}"
            for name, desc in self.AGENT_CAPABILITIES.items()
        )
        return base_prompt + "\n\n## 当前可调度 Agent 及能力\n" + capabilities_str

    def get_tools(self) -> List:
        return []

    async def analyze_scene(self, user_input: str) -> Dict[str, Any]:
        """场景识别：判断用户输入适用哪种研究场景"""
        await self._push_coordinator_thought("正在分析用户研究需求，识别适用场景...")

        if self.mock_mode:
            return {"scene": "literature_review", "confidence": 0.95,
                    "reasoning": "[MOCK] mock 模式", "keywords": ["量子力学"]}

        prompt = f"""
请分析以下用户研究需求，判断应使用哪种研究场景。

用户输入: {user_input}

四种场景：
1. literature_review — 文献综述：用户想了解某领域的现有研究、发展脉络、研究空白
2. research_proposal — 研究方案设计：用户有一个待验证的假设，需要设计研究方案
3. paper_review — 论文预审：用户提交了一段论证，需要评审
4. academic_debate — 学术辩论：用户提出了一个有争议的命题

请以 JSON 格式返回（仅返回 JSON）：
{{"scene": "场景代码", "confidence": 0.0-1.0, "reasoning": "简短理由", "keywords": ["关键词1", "关键词2"]}}
"""
        response = await self.llm.ainvoke(
            [("system", self.get_system_prompt()), ("user", prompt)]
        )
        try:
            return json.loads(response.content)
        except json.JSONDecodeError:
            return {"scene": "literature_review", "confidence": 0.5, "reasoning": "解析失败，默认文献综述", "keywords": []}

    async def decompose_task(self, user_input: str, scene: str) -> List[Dict[str, Any]]:
        """任务分解：将用户研究需求拆解为子任务"""
        await self._push_coordinator_thought(f"正在将研究任务分解为子任务 (场景: {scene})...")

        if self.mock_mode:
            return [
                {"step": 1, "agent": "theoretical-physicist", "task": f"分析理论框架: {user_input}", "expected_output": "理论分析报告"},
                {"step": 2, "agent": "experimental-physicist", "task": f"实验验证方案: {user_input}", "expected_output": "实验设计"},
            ]

        prompt = f"""

研究任务: {user_input}
研究场景: {scene}
可用 Agent: {json.dumps(self.AGENT_CAPABILITIES, ensure_ascii=False)}

请以 JSON 数组格式返回子任务列表（仅返回 JSON）：
[
  {{"step": 1, "agent": "agent_name", "task": "具体子任务描述", "expected_output": "预期产出"}},
  ...
]
"""
        response = await self.llm.ainvoke(
            [("system", self.get_system_prompt()), ("user", prompt)]
        )
        try:
            plan = json.loads(response.content)
            return plan if isinstance(plan, list) else []
        except json.JSONDecodeError:
            return []

    async def summarize_results(self, agent_outputs: Dict[str, str], scene: str, user_input: str) -> str:
        """结果汇总：整合所有 Agent 输出"""
        await self._push_coordinator_thought(
            f"正在汇总 {len(agent_outputs)} 个 Agent 的输出，整合为结构化报告..."
        )

        outputs_str = "\n\n---\n\n".join(
            f"【{name}】:\n{text[:1500]}" for name, text in agent_outputs.items()
        )

        prompt = f"""
请将以下多个 Agent 的研究输出整合为一份结构化报告。

研究主题: {user_input}
研究场景: {scene}

各 Agent 输出:
{outputs_str}

按以下结构整合为 Markdown 报告（精炼，总长控制在 2000 字内）：
1. ## 研究摘要
2. ## 核心观点汇总
3. ## 交叉分析（一致性+矛盾点）
4. ## 研究空白与后续建议
"""
        response = await self.llm.ainvoke(
            [("system", self.get_system_prompt()), ("user", prompt)]
        )
        return response.content

    async def execute(self, task: str, context: Optional[Dict[str, Any]] = None) -> str:
        """协调员执行：场景识别 → 任务分解"""
        if self.mock_mode:
            return self._mock_response(task)

        scene_info = await self.analyze_scene(task)
        sub_tasks = await self.decompose_task(task, scene_info.get("scene", "literature_review"))

        plan_output = f"""## 场景分析
- 场景: {scene_info.get('scene', 'unknown')}
- 置信度: {scene_info.get('confidence', 0)}
- 理由: {scene_info.get('reasoning', '')}

## 任务分解计划
"""
        for st in sub_tasks:
            plan_output += f"- Step {st.get('step', '?')}: **{st.get('agent', '?')}** → {st.get('task', '?')}\n"

        plan_output += "\n(注: 各 Agent 的实际执行将在 Phase 4 Workflow 引擎中完成。Route 接入完成后自动执行完整的 Task → All Agents → Summarize 流程)"

        return plan_output

    async def _push_coordinator_thought(self, message: str):
        """推送协调员的思考步骤"""
        from streaming.event_bus import push_agent_thought, get_active_bus
        if get_active_bus() is None:
            return
        await push_agent_thought(self.agent_name, message)


def create_coordinator() -> ResearchCoordinator:
    return ResearchCoordinator()