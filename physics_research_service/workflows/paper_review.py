"""
Workflow C: 论文预审 (Paper Review)

流程:
  parse_paper → parallel_review (4 agents 各从不同维度审)
  → comprehensive_review → logic_gap_detection → structured_report

A2A: 4 个 Agent 并行审同一篇论文（不同维度），
      Peer Reviewer 汇总 + Mathematical Methodologist 专项漏洞检测
"""
from langgraph.graph import StateGraph, END
from workflows.state import WorkflowState
from agents.registry import (
    get_theoretical, get_experimental, get_mathematical,
    get_interdisciplinary, get_peer_reviewer, get_coordinator,
)


async def parse_paper_node(state: WorkflowState) -> WorkflowState:
    """节点1: 论文解析 — 提取核心论点和论证结构"""
    state["current_stage"] = "parse_paper"
    coordinator = get_coordinator()
    scene_info = await coordinator.analyze_scene(state["user_input"])
    plan = await coordinator.decompose_task(
        state["user_input"], scene_info.get("scene", "paper_review")
    )
    state["scene_analysis"] = scene_info
    state["task_plan"] = plan
    return state


async def theoretical_review(state: WorkflowState) -> dict:
    """并行 A: 理论物理学家审理论框架自洽性"""
    agent = get_theoretical()
    result = await agent.execute(
        task=f"从理论物理角度评审以下论证的理论框架自洽性:\n\n{state['user_input'][:3000]}",
        context={"role": "paper_reviewer", "dimension": "theoretical_consistency"},
    )
    return {"agent_outputs": {"theoretical_review": result}}


async def experimental_review(state: WorkflowState) -> dict:
    """并行 B: 实验物理学家审实验可复现性"""
    agent = get_experimental()
    result = await agent.execute(
        task=f"从实验物理角度评审以下论证中实验设计的可复现性:\n\n{state['user_input'][:3000]}",
        context={"role": "paper_reviewer", "dimension": "experimental_reproducibility"},
    )
    return {"agent_outputs": {"experimental_review": result}}


async def mathematical_review(state: WorkflowState) -> dict:
    """并行 C: 数学方法论专家审推导严密性"""
    agent = get_mathematical()
    result = await agent.execute(
        task=f"从数学角度评审以下论证的推导严密性和公式正确性:\n\n{state['user_input'][:3000]}",
        context={"role": "paper_reviewer", "dimension": "mathematical_rigor"},
    )
    return {"agent_outputs": {"mathematical_review": result}}


async def interdisciplinary_review(state: WorkflowState) -> dict:
    """并行 D: 跨学科研究者审跨学科盲区"""
    agent = get_interdisciplinary()
    result = await agent.execute(
        task=f"从跨学科角度评审以下论证是否有学科盲区:\n\n{state['user_input'][:3000]}",
        context={"role": "paper_reviewer", "dimension": "interdisciplinary_blindspots"},
    )
    return {"agent_outputs": {"interdisciplinary_review": result}}


async def comprehensive_review_node(state: WorkflowState) -> WorkflowState:
    """节点3: 综合评审 — Peer Reviewer 汇总各维度意见"""
    state["current_stage"] = "comprehensive_review"
    reviewer = get_peer_reviewer()
    result = await reviewer.execute(
        task=f"综合各维度评审意见，给出总体评审结论: {state['user_input'][:500]}",
        context={
            "agent_outputs": {
                "理论框架评审": state["agent_outputs"].get("theoretical_review", ""),
                "实验可复现性": state["agent_outputs"].get("experimental_review", ""),
                "数学严谨性": state["agent_outputs"].get("mathematical_review", ""),
                "跨学科盲区": state["agent_outputs"].get("interdisciplinary_review", ""),
            }
        },
    )
    state["agent_outputs"]["comprehensive_review"] = result
    return state


async def logic_gap_node(state: WorkflowState) -> WorkflowState:
    """节点4: 逻辑漏洞专项检测 — Mathematical Methodologist"""
    state["current_stage"] = "logic_gap_detection"
    agent = get_mathematical()
    result = await agent.execute(
        task=f"专项检测以下论证的逻辑漏洞（循环论证、偷换概念、样本不足、因果混淆等）:\n\n{state['user_input'][:3000]}",
        context={
            "mode": "logic_gap_detection_only",
            "previous_reviews": state.get("agent_outputs", {}).get("comprehensive_review", ""),
        },
    )
    state["agent_outputs"]["logic_gap_analysis"] = result
    return state


async def generate_report_node(state: WorkflowState) -> WorkflowState:
    """节点5: 结构化审稿报告 — Coordinator 汇总"""
    state["current_stage"] = "generate_report"
    coordinator = get_coordinator()

    final = await coordinator.summarize_results(
        agent_outputs={
            "comprehensive_review": state["agent_outputs"].get("comprehensive_review", ""),
            "logic_gap_analysis": state["agent_outputs"].get("logic_gap_analysis", ""),
        },
        scene="paper_review",
        user_input=state["user_input"],
    )
    state["final_output"] = final
    return state


# ── 构建 Graph ──

def build_paper_review_workflow():
    workflow = StateGraph(WorkflowState)

    workflow.add_node("parse_paper", parse_paper_node)
    workflow.add_node("theoretical_review", theoretical_review)
    workflow.add_node("experimental_review", experimental_review)
    workflow.add_node("mathematical_review", mathematical_review)
    workflow.add_node("interdisciplinary_review", interdisciplinary_review)
    workflow.add_node("comprehensive_review", comprehensive_review_node)
    workflow.add_node("logic_gap", logic_gap_node)
    workflow.add_node("generate_report", generate_report_node)

    workflow.set_entry_point("parse_paper")

    # 并行评审
    workflow.add_edge("parse_paper", "theoretical_review")
    workflow.add_edge("parse_paper", "experimental_review")
    workflow.add_edge("parse_paper", "mathematical_review")
    workflow.add_edge("parse_paper", "interdisciplinary_review")

    # 汇聚到综合评审
    workflow.add_edge("theoretical_review", "comprehensive_review")
    workflow.add_edge("experimental_review", "comprehensive_review")
    workflow.add_edge("mathematical_review", "comprehensive_review")
    workflow.add_edge("interdisciplinary_review", "comprehensive_review")

    # 综合评审 → 逻辑漏洞 → 报告
    workflow.add_edge("comprehensive_review", "logic_gap")
    workflow.add_edge("logic_gap", "generate_report")
    workflow.add_edge("generate_report", END)

    return workflow.compile()