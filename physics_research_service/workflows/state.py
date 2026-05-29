"""
WorkflowState — 所有 Workflow 共享的状态定义
"""
from typing import TypedDict, List, Dict, Any, Optional, Annotated
from enum import Enum
import operator


class SceneType(str, Enum):
    LITERATURE_REVIEW = "literature_review"
    RESEARCH_PROPOSAL = "research_proposal"
    PAPER_REVIEW = "paper_review"
    ACADEMIC_DEBATE = "academic_debate"
    CUSTOM = "custom_workflow"


class DebatePosition(str, Enum):
    PRO = "pro"       # 正方
    CON = "con"       # 反方
    NEUTRAL = "neutral"


class WorkflowState(TypedDict, total=False):
    # ── 输入 ──
    scene_type: str
    user_input: str
    user_id: str
    research_id: str

    # ── 流程控制 ──
    current_stage: str
    revision_round: int
    max_revision_rounds: int

    # ── Coordinator 输出 ──
    scene_analysis: Dict[str, Any]
    task_plan: List[Dict[str, Any]]

    # ── Agent 输出共享 (A2A 通道) ──
    agent_outputs: Annotated[Dict[str, str], operator.or_]  # {agent_name: output_text}
    # operator.or_ 确保并行节点写入同一个 dict

    # ── 评审结果 ──
    peer_review_result: Dict[str, Any]    # 评审报告
    review_passed: bool                    # 评审是否通过

    # ── 辩论专用 ──
    debate_pro_position: Dict[str, Any]    # 正方论点
    debate_con_position: Dict[str, Any]    # 反方论点
    debate_round: int                      # 当前辩论轮次
    debate_verdict: Dict[str, Any]         # 裁决结果

    # ── 互动辩论 ──
    interactive_mode: bool                 # 是否启用互动模式 (辩论暂停等待用户)
    user_intervention: str                 # 用户插话内容
    user_stance: str                       # 用户立场: pro/conc/neutral

    # ── 最终输出 ──
    final_output: str
    error_message: Optional[str]
    knowledge_graph: Optional[Dict[str, Any]]  # {nodes: [{id, label, group}], edges: [{source, target, label}]}