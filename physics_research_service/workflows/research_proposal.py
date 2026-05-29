"""
Workflow B: 研究方案设计 (Research Proposal)

流程:
  hypothesis_clarify → parallel_draft (4 agents)
  → merge_proposal → peer_review
  → [conditional: passed? → finalize | revise → merge (loop max 3)]
  → generate_final

A2A: 4 个 Agent 并行起草各自的方案部分，
      Peer Reviewer 评审后，若不通过则各 Agent 收到评审意见修正
"""
import logging

from langgraph.graph import StateGraph, END

from workflows.state import WorkflowState
from agents.registry import (
    get_theoretical, get_experimental, get_mathematical,
    get_interdisciplinary, get_peer_reviewer, get_coordinator,
)

logger = logging.getLogger(__name__)


async def hypothesis_clarify_node(state: WorkflowState) -> WorkflowState:
    """节点1: 假设接收与澄清"""
    state["current_stage"] = "hypothesis_clarify"
    coordinator = get_coordinator()
    scene_info = await coordinator.analyze_scene(state["user_input"])
    plan = await coordinator.decompose_task(
        state["user_input"], scene_info.get("scene", "research_proposal")
    )
    state["scene_analysis"] = scene_info
    state["task_plan"] = plan
    return state


async def theoretical_draft(state: WorkflowState) -> dict:
    """并行 A: 理论物理学家 — 理论框架 + 预测"""
    agent = get_theoretical()
    previous_feedback = state.get("agent_outputs", {}).get("peer_review_result", "")
    task = f"为以下假设设计理论框架和理论预测: {state['user_input']}"
    if previous_feedback:
        task += f"\n\n[上一轮评审意见]: {previous_feedback[:500]}\n请根据意见修正理论框架。"
    result = await agent.execute(task=task)
    return {"agent_outputs": {"theoretical_physicist": result}}


async def experimental_draft(state: WorkflowState) -> dict:
    """并行 B: 实验物理学家 — 实验方案 + 测量方法"""
    agent = get_experimental()
    previous_feedback = state.get("agent_outputs", {}).get("peer_review_result", "")
    task = f"为以下假设设计实验验证方案: {state['user_input']}"
    if previous_feedback:
        task += f"\n\n[上一轮评审意见]: {previous_feedback[:500]}\n请根据意见修正实验方案。"
    result = await agent.execute(task=task)
    return {"agent_outputs": {"experimental_physicist": result}}


async def mathematical_draft(state: WorkflowState) -> dict:
    """并行 C: 数学方法论专家 — 数学模型 + 近似条件"""
    agent = get_mathematical()
    previous_feedback = state.get("agent_outputs", {}).get("peer_review_result", "")
    task = f"为以下假设构建数学模型并评估近似条件: {state['user_input']}"
    if previous_feedback:
        task += f"\n\n[上一轮评审意见]: {previous_feedback[:500]}\n请根据意见修正数学模型。"
    result = await agent.execute(task=task)
    return {"agent_outputs": {"mathematical_methodologist": result}}


async def interdisciplinary_draft(state: WorkflowState) -> dict:
    """并行 D: 跨学科研究者 — 跨学科方法论参考"""
    agent = get_interdisciplinary()
    previous_feedback = state.get("agent_outputs", {}).get("peer_review_result", "")
    task = f"为以下研究方案提供跨学科方法论参考: {state['user_input']}"
    if previous_feedback:
        task += f"\n\n[上一轮评审意见]: {previous_feedback[:500]}\n请根据意见补充跨学科视角。"
    result = await agent.execute(task=task)
    return {"agent_outputs": {"interdisciplinary_researcher": result}}


async def merge_proposal_node(state: WorkflowState) -> WorkflowState:
    """节点3: 方案整合 — Coordinator 合并初版方案"""
    state["current_stage"] = "merge_proposal"
    coordinator = get_coordinator()
    merged = await coordinator.summarize_results(
        agent_outputs=state.get("agent_outputs", {}),
        scene="research_proposal",
        user_input=state["user_input"],
    )
    state["agent_outputs"]["merged_proposal"] = merged
    return state


async def peer_review_node(state: WorkflowState) -> dict:
    """节点4: 内部评审 — Peer Reviewer 评估 + 轮次控制"""
    state["current_stage"] = "peer_review"
    
    # ⚠️ 先自增轮次（在节点函数中修改才生效；不可在条件路由函数中修改 state）
    current_round = state.get("revision_round", 0) + 1
    max_rounds = state.get("max_revision_rounds", 3)
    
    reviewer = get_peer_reviewer()
    review_inputs = {"merged_proposal": state["agent_outputs"].get("merged_proposal", "")}
    review_inputs.update(state.get("agent_outputs", {}))

    result = await reviewer.execute(
        task=f"评审以下研究方案的可行性、创新性和严谨性: {state['user_input']}",
        context={"agent_outputs": review_inputs},
    )

    # 达到最大轮次自动通过，避免无限循环
    if current_round >= max_rounds:
        return {
            "agent_outputs": {"peer_review_result": result},
            "current_stage": "peer_review",
            "revision_round": current_round,
            "review_passed": True,
        }

    # 简单判断：包含通过关键词则通过，否则继续修订
    result_lower = result.lower()
    passed = "通过" in result_lower and "不通过" not in result_lower

    return {
        "agent_outputs": {"peer_review_result": result},
        "current_stage": "peer_review",
        "revision_round": current_round,
        "review_passed": passed,
    }


def should_revise(state: WorkflowState) -> str:
    """条件路由: 评审是否通过？(只读，不修改 state)"""
    if state.get("review_passed", False):
        return "finalize"
    if state.get("revision_round", 0) < state.get("max_revision_rounds", 3):
        return "revise"
    return "finalize"


async def finalize_node(state: WorkflowState) -> WorkflowState:
    """节点6: 最终方案输出"""
    state["current_stage"] = "finalize"
    coordinator = get_coordinator()

    final = await coordinator.summarize_results(
        agent_outputs={
            "merged_proposal": state["agent_outputs"].get("merged_proposal", ""),
            "peer_review_result": state["agent_outputs"].get("peer_review_result", ""),
        },
        scene="research_proposal",
        user_input=state["user_input"],
    )
    state["final_output"] = final
    return state


# ── 修订网关 (无操作，仅扇出) ──

async def retry_gateway(state: WorkflowState) -> WorkflowState:
    """修订入口，扇出到4个并行草拟节点"""
    state["current_stage"] = "revise_gateway"
    logger.info("[Revision] 第 %d 轮修订开始", state.get("revision_round", 0))
    return state


# ── 构建 Graph ──

def build_research_proposal_workflow():
    workflow = StateGraph(WorkflowState)

    workflow.add_node("hypothesis_clarify", hypothesis_clarify_node)
    workflow.add_node("theoretical_draft", theoretical_draft)
    workflow.add_node("experimental_draft", experimental_draft)
    workflow.add_node("mathematical_draft", mathematical_draft)
    workflow.add_node("interdisciplinary_draft", interdisciplinary_draft)
    workflow.add_node("merge_proposal", merge_proposal_node)
    workflow.add_node("peer_review", peer_review_node)
    workflow.add_node("finalize", finalize_node)
    workflow.add_node("retry_gateway", retry_gateway)

    workflow.set_entry_point("hypothesis_clarify")

    # 首次：假设澄清 → 四个并行草拟
    workflow.add_edge("hypothesis_clarify", "theoretical_draft")
    workflow.add_edge("hypothesis_clarify", "experimental_draft")
    workflow.add_edge("hypothesis_clarify", "mathematical_draft")
    workflow.add_edge("hypothesis_clarify", "interdisciplinary_draft")

    # 修订网关 → 四个并行草拟 (与首次共用草拟节点)
    workflow.add_edge("retry_gateway", "theoretical_draft")
    workflow.add_edge("retry_gateway", "experimental_draft")
    workflow.add_edge("retry_gateway", "mathematical_draft")
    workflow.add_edge("retry_gateway", "interdisciplinary_draft")

    # 汇聚到 merge
    workflow.add_edge("theoretical_draft", "merge_proposal")
    workflow.add_edge("experimental_draft", "merge_proposal")
    workflow.add_edge("mathematical_draft", "merge_proposal")
    workflow.add_edge("interdisciplinary_draft", "merge_proposal")

    # merge → review → 条件分支
    workflow.add_edge("merge_proposal", "peer_review")
    workflow.add_conditional_edges(
        "peer_review",
        should_revise,
        {
            "revise": "retry_gateway",  # 通过网关扇出到4个草拟节点
            "finalize": "finalize",
        }
    )

    workflow.add_edge("finalize", END)

    return workflow.compile()