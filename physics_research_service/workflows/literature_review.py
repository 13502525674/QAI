"""
Workflow A: 文献综述 (Literature Review)

流程:
  parse_topic → parallel_search (theoretical + experimental + interdisciplinary)
  → cross_compare → build_knowledge_graph → identify_gaps → generate_report

A2A: 3 个 Agent 并行检索，结果存入 agent_outputs，下游节点读取汇总
"""
import re
from langgraph.graph import StateGraph, END
from workflows.state import WorkflowState
from agents.registry import get_theoretical, get_experimental, get_interdisciplinary, get_peer_reviewer, get_coordinator


async def parse_topic_node(state: WorkflowState) -> WorkflowState:
    """节点1: 主题解析 → Coordinator 分析场景和关键词"""
    state["current_stage"] = "parse_topic"
    coordinator = get_coordinator()
    scene_info = await coordinator.analyze_scene(state["user_input"])
    plan = await coordinator.decompose_task(state["user_input"], scene_info.get("scene", "literature_review"))
    state["scene_analysis"] = scene_info
    state["task_plan"] = plan
    return state


async def theoretical_search(state: WorkflowState) -> dict:
    """并行分支 A: 理论物理学家搜索理论流派发展脉络"""
    agent = get_theoretical()
    result = await agent.execute(
        task=f"检索并分析以下主题的理论发展脉络: {state['user_input']}",
        context={"stage": "literature_search", "focus": "theoretical_development"},
    )
    return {"agent_outputs": {"theoretical_physicist": result}}


async def experimental_search(state: WorkflowState) -> dict:
    """并行分支 B: 实验物理学家搜索实验验证相关文献"""
    agent = get_experimental()
    result = await agent.execute(
        task=f"检索与以下主题相关的实验验证方法和实验数据: {state['user_input']}",
        context={"stage": "literature_search", "focus": "experimental_validation"},
    )
    return {"agent_outputs": {"experimental_physicist": result}}


async def interdisciplinary_search(state: WorkflowState) -> dict:
    """并行分支 C: 跨学科研究者搜索跨学科关联"""
    agent = get_interdisciplinary()
    result = await agent.execute(
        task=f"从跨学科角度分析以下主题的相关研究: {state['user_input']}",
        context={"stage": "literature_search", "focus": "cross_domain"},
    )
    return {"agent_outputs": {"interdisciplinary_researcher": result}}


async def cross_compare_node(state: WorkflowState) -> WorkflowState:
    """节点3: 文献交叉比对 — Peer Reviewer 去重分类"""
    state["current_stage"] = "cross_compare"
    reviewer = get_peer_reviewer()
    outputs = state.get("agent_outputs", {})
    result = await reviewer.execute(
        task="对上述三个 Agent 的文献检索结果进行交叉比对：去重、按时间线/学派分类、识别一致性结论和矛盾之处",
        context={
            "agent_outputs": outputs,
            "task": state["user_input"],
        },
    )
    state["agent_outputs"]["peer_review_cross_compare"] = result
    return state


async def build_kg_node(state: WorkflowState) -> WorkflowState:
    """节点4: 知识图谱构建 — 抽取实体与关系"""
    state["current_stage"] = "build_knowledge_graph"
    coordinator = get_coordinator()

    # 生成知识图谱结构化数据
    kg = await _generate_knowledge_graph(
        user_input=state["user_input"],
        agent_outputs=state.get("agent_outputs", {}),
        scene_analysis=state.get("scene_analysis", {}),
        mock_mode=coordinator.mock_mode,
    )
    state["knowledge_graph"] = kg

    # 仍然生成最终报告
    combined = "\n\n---\n\n".join(
        f"【{name}】:\n{text[:2000]}"
        for name, text in state.get("agent_outputs", {}).items()
    )

    summary = await coordinator.summarize_results(
        agent_outputs=state.get("agent_outputs", {}),
        scene="literature_review",
        user_input=state["user_input"],
    )

    state["final_output"] = summary
    return state


async def _generate_knowledge_graph(
    user_input: str,
    agent_outputs: dict,
    scene_analysis: dict,
    mock_mode: bool = False,
) -> dict:
    """从 Agent 输出中提取知识图谱 (节点 + 关系)

    mock_mode: 返回预构建图谱，不调 LLM
    正式模式: 调用 Coordinator LLM 提取结构化实体关系
    """
    if mock_mode:
        return _build_mock_kg(user_input, agent_outputs)

    # 正式模式: 调用 LLM 提取
    from config.settings import get_settings
    from langchain_openai import ChatOpenAI

    s = get_settings()
    llm = ChatOpenAI(
        model=s.dashscope_model_name,
        openai_api_key=s.dashscope_api_key,
        openai_api_base=s.dashscope_base_url,
        temperature=0.1,
    )

    combined_text = "\n\n".join(
        f"[{name}] {text[:1500]}"
        for name, text in agent_outputs.items()
    )

    prompt = f"""请从以下物理研究分析中提取知识图谱，返回纯 JSON。

研究主题: {user_input}

分析内容:
{combined_text}

请提取：
1. 核心概念/理论/人物/实验 (作为 nodes)
2. 概念之间的关系 (作为 edges)，关系类型如: "提出", "验证", "属于", "矛盾", "推导自", "支持", "替代"

以 JSON 格式返回 (仅 JSON，无 Markdown):
{{
  "nodes": [
    {{"id": "node1", "label": "显示名称", "group": "theory|experiment|person|concept"}},
    ...
  ],
  "edges": [
    {{"source": "node1", "target": "node2", "label": "关系描述"}},
    ...
  ]
}}"""

    try:
        resp = await llm.ainvoke(prompt)
        content = resp.content.strip()
        # 清理 markdown 包裹
        if content.startswith("```"):
            content = re.sub(r"^```\w*\n?", "", content)
            content = re.sub(r"\n?```$", "", content)
        import json
        kg = json.loads(content)
        return kg if isinstance(kg, dict) else {}
    except Exception:
        return _build_mock_kg(user_input, agent_outputs)


def _build_mock_kg(user_input: str, agent_outputs: dict = None) -> dict:
    """构建模拟知识图谱 (零 Token 消耗)"""
    # 从输入中提取关键词作为节点
    topic_keywords = [
        "量子力学", "广义相对论", "标准模型", "弦理论", "圈量子引力",
        "黑洞", "暗物质", "暗能量", "量子纠缠", "超对称",
        "AdS/CFT", "全息原理", "引力波", "中微子", "希格斯玻色子",
        "量子场论", "拓扑", "超导", "量子计算", "大统一理论",
    ]

    topic_lower = user_input.lower()
    matched = [kw for kw in topic_keywords if any(
        c in topic_lower for c in [kw.lower(), kw[:2].lower()]
    )]

    # 保证至少 6-12 个节点
    if len(matched) < 6:
        matched = matched + topic_keywords[:8 - len(matched)]

    groups = ["theory", "experiment", "person", "concept"]
    nodes = []
    for i, kw in enumerate(matched[:12]):
        nodes.append({
            "id": f"n{i}",
            "label": kw,
            "group": groups[i % 4],
            "symbolSize": 30 + (12 - i) * 3,
        })

    # 预设关系
    edge_templates = [
        ("提出", "person", "theory"),
        ("验证", "experiment", "theory"),
        ("属于", "concept", "theory"),
        ("矛盾", "theory", "theory"),
        ("推导自", "theory", "theory"),
        ("支持", "experiment", "concept"),
        ("替代", "theory", "theory"),
        ("包含", "concept", "concept"),
    ]

    import random
    random.seed(42)
    edges = []
    for i in range(len(nodes)):
        for j in range(i + 1, min(i + 4, len(nodes))):
            tmpl = random.choice(edge_templates)
            edges.append({
                "source": nodes[i]["id"],
                "target": nodes[j]["id"],
                "label": tmpl[0],
            })

    return {"nodes": nodes, "edges": edges}


# ── 构建 Graph ──

def build_literature_review_workflow():
    workflow = StateGraph(WorkflowState)

    # 添加节点
    workflow.add_node("parse_topic", parse_topic_node)
    workflow.add_node("theoretical_search", theoretical_search)
    workflow.add_node("experimental_search", experimental_search)
    workflow.add_node("interdisciplinary_search", interdisciplinary_search)
    workflow.add_node("cross_compare", cross_compare_node)
    workflow.add_node("generate_report", build_kg_node)

    # 设置入口
    workflow.set_entry_point("parse_topic")

    # parse_topic → 并行分叉到三个 Agent
    workflow.add_edge("parse_topic", "theoretical_search")
    workflow.add_edge("parse_topic", "experimental_search")
    workflow.add_edge("parse_topic", "interdisciplinary_search")

    # 三个 Agent 全部完成后 → 汇聚到 cross_compare
    workflow.add_edge("theoretical_search", "cross_compare")
    workflow.add_edge("experimental_search", "cross_compare")
    workflow.add_edge("interdisciplinary_search", "cross_compare")

    # cross_compare → generate_report → END
    workflow.add_edge("cross_compare", "generate_report")
    workflow.add_edge("generate_report", END)

    return workflow.compile()