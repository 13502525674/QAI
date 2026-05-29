"""
Workflow D: 学术辩论 (Academic Debate)

流程:
  decompose_proposition → assign_positions
  → round1_opening (pro + con parallel) → round2_interrogation (parallel)
  → round3_rebuttal (parallel) → peer_judgment → consensus_summary

A2A: 正方 Agent 和反方 Agent 通过 state 交换论点，
      Peer Reviewer 做最终裁决
"""
from langgraph.graph import StateGraph, END
from workflows.state import WorkflowState
from agents.registry import (
    get_theoretical, get_experimental, get_interdisciplinary,
    get_peer_reviewer, get_coordinator,
)


async def decompose_proposition_node(state: WorkflowState) -> WorkflowState:
    """节点1: 命题分解"""
    state["current_stage"] = "decompose_proposition"
    state["debate_round"] = 0
    coordinator = get_coordinator()
    scene_info = await coordinator.analyze_scene(state["user_input"])
    plan = await coordinator.decompose_task(
        state["user_input"], scene_info.get("scene", "academic_debate")
    )
    state["scene_analysis"] = scene_info
    state["task_plan"] = plan

    # 初始化辩论状态
    state["debate_pro_position"] = {"arguments": [], "support": 0}
    state["debate_con_position"] = {"arguments": [], "support": 0}
    return state


# ── 正方 Agent (Theoretical + Interdisciplinary) ──

async def pro_opening(state: WorkflowState) -> dict:
    """正方立论"""
    pro_args = []
    # Theoretical 主立论
    t = get_theoretical()
    r_t = await t.execute(
        task=f"作为正方，为以下命题构建立论（支持该命题）:\n{state['user_input']}\n请给出3个核心论点，每个论点附证据或推理链。"
    )
    pro_args.append(f"[理论物理学家 · 正方]:\n{r_t}")

    # Interdisciplinary 补充跨学科视角
    inter = get_interdisciplinary()
    r_inter = await inter.execute(
        task=f"作为正方，从跨学科角度为以下命题提供补充支持:\n{state['user_input']}"
    )
    pro_args.append(f"[跨学科研究者 · 正方]:\n{r_inter}")

    pro_text = "\n\n---\n\n".join(pro_args)
    return {
        "agent_outputs": {"pro_opening": pro_text},
        "debate_pro_position": {"arguments": pro_args, "support": 2},
    }


# ── 反方 Agent (Experimental + Interdisciplinary) ──

async def con_opening(state: WorkflowState) -> dict:
    """反方立论"""
    con_args = []
    # Experimental 主质疑
    e = get_experimental()
    r_e = await e.execute(
        task=f"作为反方，对以下命题构建反驳（反对该命题）:\n{state['user_input']}\n请从实验证据不足、方法论缺陷等角度提出3个质疑。"
    )
    con_args.append(f"[实验物理学家 · 反方]:\n{r_e}")

    # Interdisciplinary 补充跨学科质疑
    inter = get_interdisciplinary()
    r_inter = await inter.execute(
        task=f"作为反方，从跨学科角度对以下命题提出质疑:\n{state['user_input']}"
    )
    con_args.append(f"[跨学科研究者 · 反方]:\n{r_inter}")

    con_text = "\n\n---\n\n".join(con_args)
    return {
        "agent_outputs": {"con_opening": con_text},
        "debate_con_position": {"arguments": con_args, "support": 2},
    }


async def pro_interrogation(state: WorkflowState) -> dict:
    """正方质询反方论点"""
    con_args = state.get("debate_con_position", {}).get("arguments", [])
    con_text = "\n".join(con_args) if con_args else "(无)"

    t = get_theoretical()
    result = await t.execute(
        task=f"作为正方，逐条质询反方的以下论点:\n{con_text[:2000]}\n\n原命题: {state['user_input']}"
    )
    return {"agent_outputs": {"pro_interrogation": result}}


async def con_interrogation(state: WorkflowState) -> dict:
    """反方质询正方论点"""
    pro_args = state.get("debate_pro_position", {}).get("arguments", [])
    pro_text = "\n".join(pro_args) if pro_args else "(无)"

    e = get_experimental()
    result = await e.execute(
        task=f"作为反方，逐条质询正方的以下论点:\n{pro_text[:2000]}\n\n原命题: {state['user_input']}"
    )
    return {"agent_outputs": {"con_interrogation": result}}


async def pro_rebuttal(state: WorkflowState) -> dict:
    """正方回应"""
    con_inter = state.get("agent_outputs", {}).get("con_interrogation", "")
    t = get_theoretical()
    result = await t.execute(
        task=f"作为正方，逐一回应反方质询:\n{con_inter[:2000]}\n\n原命题: {state['user_input']}"
    )
    return {"agent_outputs": {"pro_rebuttal": result}}


async def con_rebuttal(state: WorkflowState) -> dict:
    """反方回应"""
    pro_inter = state.get("agent_outputs", {}).get("pro_interrogation", "")
    e = get_experimental()
    result = await e.execute(
        task=f"作为反方，逐一回应正方质询:\n{pro_inter[:2000]}\n\n原命题: {state['user_input']}"
    )
    return {"agent_outputs": {"con_rebuttal": result}}


async def peer_judgment_node(state: WorkflowState) -> WorkflowState:
    """Peer Reviewer 裁决"""
    state["current_stage"] = "peer_judgment"
    reviewer = get_peer_reviewer()

    debate_record = f"""
## 正方立论
{state.get('agent_outputs', {}).get('pro_opening', '')}

## 反方立论
{state.get('agent_outputs', {}).get('con_opening', '')}

## 正方质询
{state.get('agent_outputs', {}).get('pro_interrogation', '')}

## 反方质询
{state.get('agent_outputs', {}).get('con_interrogation', '')}

## 正方回应
{state.get('agent_outputs', {}).get('pro_rebuttal', '')}

## 反方回应
{state.get('agent_outputs', {}).get('con_rebuttal', '')}
"""
    result = await reviewer.execute(
        task=f"作为学术辩论裁判，评估以下辩论记录并给出裁决:\n原命题: {state['user_input']}\n\n{debate_record[:4000]}",
        context={
            "agent_outputs": {
                "完整辩论记录": debate_record[:3000],
            }
        },
    )
    state["agent_outputs"]["peer_judgment"] = result
    return state


async def consensus_summary_node(state: WorkflowState) -> WorkflowState:
    """共识与分歧总结 — Coordinator 汇总"""
    state["current_stage"] = "consensus_summary"
    coordinator = get_coordinator()

    debate_outputs = {
        "正方立论": state["agent_outputs"].get("pro_opening", ""),
        "反方立论": state["agent_outputs"].get("con_opening", ""),
        "裁判裁决": state["agent_outputs"].get("peer_judgment", ""),
    }

    final = await coordinator.summarize_results(
        agent_outputs=debate_outputs,
        scene="academic_debate",
        user_input=state["user_input"],
    )
    state["final_output"] = final
    return state


# ── 构建 Graph ──

def build_academic_debate_workflow():
    workflow = StateGraph(WorkflowState)

    workflow.add_node("decompose_proposition", decompose_proposition_node)
    workflow.add_node("pro_opening", pro_opening)
    workflow.add_node("con_opening", con_opening)
    workflow.add_node("pro_interrogation", pro_interrogation)
    workflow.add_node("con_interrogation", con_interrogation)
    workflow.add_node("pro_rebuttal", pro_rebuttal)
    workflow.add_node("con_rebuttal", con_rebuttal)
    workflow.add_node("peer_judgment", peer_judgment_node)
    workflow.add_node("consensus_summary", consensus_summary_node)

    workflow.set_entry_point("decompose_proposition")

    # 第一轮：正反方并行立论
    workflow.add_edge("decompose_proposition", "pro_opening")
    workflow.add_edge("decompose_proposition", "con_opening")

    # 如果没设置 condition，pro/con 会并行执行，但质询需要等待两者完成
    # 使用 add_edge 从两者指向质询节点即可实现汇聚等待
    workflow.add_edge("pro_opening", "pro_interrogation")
    workflow.add_edge("con_opening", "con_interrogation")

    # 质询 → 同时汇聚到 rebuttal（但 rebuttal 需要对方质询内容）
    # 将 rebuttal 设为 pro/con interrogation 都完成后才执行
    # pro_rebuttal 依赖 con_interrogation 的内容
    # 这里简单处理：两者都汇聚到一个同步节点
    workflow.add_edge("pro_interrogation", "pro_rebuttal")
    workflow.add_edge("con_interrogation", "con_rebuttal")

    # rebuttal → 裁决
    workflow.add_edge("pro_rebuttal", "peer_judgment")
    workflow.add_edge("con_rebuttal", "peer_judgment")

    # 裁决 → 总结
    workflow.add_edge("peer_judgment", "consensus_summary")
    workflow.add_edge("consensus_summary", END)

    return workflow.compile()