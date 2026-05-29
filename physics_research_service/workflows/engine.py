"""
Workflow Engine — 管理所有 Workflow 模板的注册与执行

A2A 通信机制：
  - Agent 间通过 WorkflowState.agent_outputs 共享结果
  - 下游 Agent 读取上游 Agent 的输出作为 context
  - Coordinator 负责任务分解与最终汇总

Phase 6: 通过 StreamEventBus 实现 Agent 思考过程的实时流式输出
  - Workflow 在后台 asyncio.Task 中运行
  - Agent 通过 contextvar 向全局事件队列推送思考步骤
  - 引擎并发消费事件队列，yield SSE 事件
"""
from typing import Dict, Optional, AsyncGenerator, Any
import asyncio
import traceback

from workflows.state import WorkflowState, SceneType
from streaming.event_bus import StreamEventBus, set_active_bus, get_active_bus
from streaming.event_bus import agent_label_map


class WorkflowEngine:
    """Workflow 引擎：注册、选择、执行 Workflow 模板"""

    def __init__(self):
        self._workflows: Dict[str, Any] = {}
        self._initialized = False

    def _ensure_initialized(self):
        if self._initialized:
            return
        # 延迟导入避免循环依赖
        from workflows.literature_review import build_literature_review_workflow
        from workflows.research_proposal import build_research_proposal_workflow
        from workflows.paper_review import build_paper_review_workflow
        from workflows.academic_debate import build_academic_debate_workflow

        self._workflows = {
            SceneType.LITERATURE_REVIEW: build_literature_review_workflow,
            SceneType.RESEARCH_PROPOSAL: build_research_proposal_workflow,
            SceneType.PAPER_REVIEW: build_paper_review_workflow,
            SceneType.ACADEMIC_DEBATE: build_academic_debate_workflow,
        }
        self._initialized = True

    def list_workflows(self) -> Dict[str, str]:
        self._ensure_initialized()
        return {
            SceneType.LITERATURE_REVIEW: "文献综述 — 结构化综述报告、知识图谱、研究空白识别",
            SceneType.RESEARCH_PROPOSAL: "研究方案设计 — 完整研究计划书、含评审修订循环",
            SceneType.PAPER_REVIEW: "论文预审 — 逐段审稿意见、逻辑漏洞检测、改进建议",
            SceneType.ACADEMIC_DEBATE: "学术辩论 — 正反方三轮辩论、共识/分歧总结",
        }

    def get_workflow(self, scene_type: str):
        """获取编译好的 Workflow Graph"""
        self._ensure_initialized()
        builder = self._workflows.get(scene_type)
        if builder is None:
            raise ValueError(f"未知场景类型: {scene_type}。可用: {list(self._workflows.keys())}")
        return builder()

    async def execute(self, scene_type: str, user_input: str,
                      user_id: str = "anonymous",
                      fast_mode: bool = False) -> WorkflowState:
        """完整执行一个 Workflow（非流式，返回最终状态）"""
        workflow = self.get_workflow(scene_type)

        initial_state: WorkflowState = {
            "scene_type": scene_type,
            "user_input": user_input,
            "user_id": user_id,
            "research_id": "",
            "current_stage": "start",
            "revision_round": 0,
            "max_revision_rounds": 1 if fast_mode else 3,
            "agent_outputs": {},
            "review_passed": False,
        }

        final_state = None
        async for event in workflow.astream(initial_state):
            # event is {node_name: output_state}
            for node_name, state in event.items():
                final_state = state

        return final_state or initial_state

    async def execute_stream(self, scene_type: str, user_input: str,
                             user_id: str = "anonymous",
                             fast_mode: bool = False) -> AsyncGenerator:
        """流式执行 Workflow，逐事件 yield SSE（含 Agent 思考过程）"""
        workflow = self.get_workflow(scene_type)

        import uuid
        research_id = uuid.uuid4().hex[:12]

        enriched_input = user_input
        try:
            from memory.short_term_memory import get_user_context
            ctx = get_user_context(user_id, max_sessions=3)
            if ctx:
                enriched_input = f"{user_input}\n\n{ctx}"
        except Exception:
            pass

        initial_state: WorkflowState = {
            "scene_type": scene_type,
            "user_input": enriched_input,
            "user_id": user_id,
            "research_id": research_id,
            "current_stage": "start",
            "revision_round": 0,
            "max_revision_rounds": 1 if fast_mode else 3,
            "agent_outputs": {},
            "review_passed": False,
        }

        bus = StreamEventBus()
        set_active_bus(bus)

        yield {
            "event": "stage",
            "data": {
                "stage": "start",
                "message": f"开始 {scene_type} 工作流 (id={research_id})",
                "status": "running",
                "research_id": research_id,
            }
        }

        workflow_task = asyncio.create_task(
            self._run_workflow_to_bus(workflow, initial_state),
            name=f"wf_{research_id}"
        )

        final_state = None
        try:
            while not workflow_task.done():
                event = await bus.get(timeout=0.3)
                if event is not None:
                    yield event

            final_state = await workflow_task

            remaining = await bus.get_all_pending()
            for event in remaining:
                yield event

            if final_state:
                yield {
                    "event": "done",
                    "data": {
                        "research_id": research_id,
                        "scene_type": scene_type,
                        "user_input": user_input,
                        "output": final_state.get("final_output", ""),
                        "knowledge_graph": final_state.get("knowledge_graph"),
                    }
                }

                _save_to_memory(research_id, scene_type, user_input, user_id,
                                final_state.get("final_output", ""),
                                final_state.get("agent_outputs", {}))

        except asyncio.CancelledError:
            pass
        except Exception as e:
            traceback.print_exc()
            yield {"event": "error", "data": {"message": f"Workflow 执行失败: {str(e)[:200]}"}}
        finally:
            set_active_bus(None)
            bus.mark_done()
            if not workflow_task.done():
                workflow_task.cancel()

    async def _run_workflow_to_bus(self, workflow, initial_state: WorkflowState) -> Optional[WorkflowState]:
        """在后台运行 Workflow，将节点事件推送到事件总线"""
        final_state = None
        bus = get_active_bus()
        async for event in workflow.astream(initial_state):
            for node_name, state in event.items():
                final_state = state
                stage_info = self._node_to_sse(node_name, state)
                if stage_info and bus:
                    await bus.push(stage_info)
        return final_state

    async def execute_custom_stream(
        self, research_id: str, user_input: str, user_id: str,
        config: dict, fast_mode: bool = False,
    ) -> AsyncGenerator:
        """执行自定义编排的工作流 (动态管线) — 含 Agent 思考流式"""
        from agents.registry import (
            get_theoretical, get_experimental, get_mathematical,
            get_interdisciplinary, get_peer_reviewer, get_coordinator,
        )

        bus = StreamEventBus()
        set_active_bus(bus)

        agent_map = {
            "theoretical_physicist": get_theoretical,
            "experimental_physicist": get_experimental,
            "mathematical_methodologist": get_mathematical,
            "interdisciplinary_researcher": get_interdisciplinary,
            "peer_reviewer": get_peer_reviewer,
        }

        sequential_agents = config.get("agents", [])
        parallel_groups = config.get("parallel_groups", [])

        # 构建执行扁平列表: [(agent_name, is_parallel_group)]
        all_outputs = {}
        flat_plan = []
        p_idx = 0

        for name in sequential_agents:
            # 检查是否属于并行组
            is_parallel = False
            for pg in parallel_groups:
                if name in pg:
                    if p_idx < len(parallel_groups) and parallel_groups[p_idx] == pg:
                        is_parallel = True
                        if not flat_plan or flat_plan[-1] != ("__parallel__", pg):
                            flat_plan.append(("__parallel__", pg))
                    break
            if not is_parallel:
                flat_plan.append((name, None))
            else:
                # 并行组已添加跳过单个
                pass

        if not flat_plan:
            # fallback: 顺序执行
            flat_plan = [(n, None) for n in sequential_agents]

        yield _sse("stage", stage="start", message=f"开始自定义工作流 (id={research_id})", status="running")

        try:
            for step_name, parallel_group in flat_plan:
                if step_name == "__parallel__":
                    yield _sse("stage", stage="parallel_start",
                               message=f"并行执行: {', '.join(parallel_group)}", status="running")

                    async def run_agent(name):
                        task = f"研究任务: {user_input}\n此前结论:\n{_fmt_outputs(all_outputs)}"
                        ctx = {"agent_outputs": all_outputs, "task": user_input}
                        ag = agent_map[name]()
                        return name, await ag.execute(task=task, context=ctx)

                    results = await asyncio.gather(*[run_agent(n) for n in parallel_group])
                    for name, result in results:
                        all_outputs[name] = result
                        yield _sse("stage", stage=name, status="completed",
                                   agent=name, output_preview=result[:200])

                    if p_idx < len(parallel_groups):
                        p_idx += 1

                else:
                    yield _sse("stage", stage=step_name, status="running",
                               message=f"执行: {step_name}")
                    task = f"研究任务: {user_input}\n此前结论:\n{_fmt_outputs(all_outputs)}"
                    ctx = {"agent_outputs": all_outputs, "task": user_input}
                    ag = agent_map[step_name]()
                    result = await ag.execute(task=task, context=ctx)
                    all_outputs[step_name] = result
                    yield _sse("stage", stage=step_name, status="completed",
                               agent=step_name, output_preview=result[:200])

                pending = await bus.get_all_pending()
                for event in pending:
                    yield event

            coordinator = get_coordinator()
            summary = await coordinator.summarize_results(all_outputs, "custom", user_input)

            pending = await bus.get_all_pending()
            for event in pending:
                yield event

            yield _sse("done", research_id=research_id, scene_type="custom",
                       user_input=user_input, output=summary,
                       knowledge_graph=None)

            _save_to_memory(research_id, "custom", user_input, user_id, summary, all_outputs)

        except Exception as e:
            traceback.print_exc()
            yield _sse("error", message=f"自定义工作流失败: {str(e)[:200]}")
        finally:
            set_active_bus(None)
            bus.mark_done()

    async def execute_interactive_debate(
        self, research_id: str, user_input: str, user_id: str,
    ) -> AsyncGenerator:
        """互动式学术辩论: 分轮次执行，每轮暂停等待用户反馈 — 含 Agent 思考流式"""
        from agents.registry import (
            get_theoretical, get_experimental, get_mathematical,
            get_interdisciplinary, get_peer_reviewer, get_coordinator,
        )

        bus = StreamEventBus()
        set_active_bus(bus)

        session = DebateSession(research_id, user_input, user_id)
        _debate_sessions[research_id] = session

        try:
            yield _sse("stage", stage="decompose_proposition",
                       message="正在分析辩论命题...", status="running")
            coordinator = get_coordinator()
            plan = await coordinator.decompose_task(user_input, "academic_debate")
            yield _sse("stage", stage="decompose_proposition",
                       message="命题分析完成", status="completed")

            for event in await bus.get_all_pending():
                yield event

            yield _sse("stage", stage="round1_opening",
                       message="第一轮: 正反方立论...", status="running")

            t = get_theoretical(); e = get_experimental()
            pro_r, con_r = await asyncio.gather(
                t.execute(task=f"正方立论，支持命题: {user_input}"),
                e.execute(task=f"反方立论，反对命题: {user_input}"),
            )
            session.all_outputs["pro_opening"] = pro_r
            session.all_outputs["con_opening"] = con_r

            for event in await bus.get_all_pending():
                yield event

            yield _sse("stage", stage="round1_complete",
                       message="第一轮完成: 正反方已立论",
                       status="await_input",
                       pro_opening=pro_r[:500],
                       con_opening=con_r[:500])

            yield _sse("await_intervention", round=1,
                       message="您可在此发表评论或选择立场后继续。点击「继续辩论」跳过。",
                       position=user_input)
            try:
                await asyncio.wait_for(session.resume_event.wait(), timeout=120)
            except asyncio.TimeoutError:
                pass

            intervention = session.user_intervention
            if intervention:
                yield _sse("stage", stage="user_intervention",
                           message=f"用户介入: {intervention[:200]}",
                           status="completed")

            yield _sse("stage", stage="round2_interrogation",
                       message="第二轮: 正反方质询...", status="running")

            ctx = f"用户反馈: {intervention}" if intervention else ""
            pro_r2, con_r2 = await asyncio.gather(
                t.execute(task=f"正方质询反方论点。{ctx}\n反方论点: {con_r[:1000]}"),
                e.execute(task=f"反方质询正方论点。{ctx}\n正方论点: {pro_r[:1000]}"),
            )
            session.all_outputs["pro_interrogation"] = pro_r2
            session.all_outputs["con_interrogation"] = con_r2

            for event in await bus.get_all_pending():
                yield event

            yield _sse("stage", stage="round2_complete",
                       message="第二轮完成: 质询结束",
                       status="await_input")

            yield _sse("await_intervention", round=2,
                       message="质询结束。请发表评论或选择立场后继续。点击「继续辩论」跳过。")
            try:
                await asyncio.wait_for(session.resume_event.wait(), timeout=120)
            except asyncio.TimeoutError:
                pass

            intervention2 = session.user_intervention

            yield _sse("stage", stage="round3_rebuttal",
                       message="第三轮: 正反方反驳 + 最终陈述...", status="running")

            ctx2 = f"用户评论: {intervention2}" if intervention2 else ""
            pro_r3, con_r3 = await asyncio.gather(
                t.execute(task=f"正方最终反驳 + 总结。{ctx2}\n质询要点: {pro_r2[:800]}"),
                e.execute(task=f"反方最终反驳 + 总结。{ctx2}\n质询要点: {con_r2[:800]}"),
            )
            session.all_outputs["pro_rebuttal"] = pro_r3
            session.all_outputs["con_rebuttal"] = con_r3

            for event in await bus.get_all_pending():
                yield event

            yield _sse("stage", stage="round3_complete",
                       message="第三轮完成: 辩论结束",
                       status="await_verdict")

            yield _sse("await_verdict",
                       message="请给出您的裁决 (支持正方/支持反方/中立) 或点击「跳过裁决」")
            try:
                await asyncio.wait_for(session.resume_event.wait(), timeout=180)
            except asyncio.TimeoutError:
                pass

            user_stance = session.user_stance

            yield _sse("stage", stage="judgment",
                       message="裁判正在综合评审...", status="running")

            reviewer = get_peer_reviewer()
            debate_record = f"""# 学术辩论记录
命题: {user_input}

## 正方立论
{pro_r[:1500]}
## 反方立论
{con_r[:1500]}
## 正方质询
{pro_r2[:1000]}
## 反方质询
{con_r2[:1000]}
## 正方反驳
{pro_r3[:1000]}
## 反方反驳
{con_r3[:1000]}
## 用户裁决: {user_stance}
"""
            judgment = await reviewer.execute(
                task=f"作为辩论裁判，评估以下辩论并给出综合裁决:\n{debate_record[:5000]}"
            )

            for event in await bus.get_all_pending():
                yield event

            summary = await coordinator.summarize_results(
                {
                    "正方立论": pro_r, "反方立论": con_r,
                    "裁判裁决": judgment,
                    "用户立场": user_stance,
                },
                "academic_debate", user_input,
            )

            for event in await bus.get_all_pending():
                yield event

            yield _sse("done",
                       research_id=research_id, scene_type="academic_debate",
                       user_input=user_input, output=summary,
                       knowledge_graph=None,
                       judgment=judgment[:500],
                       user_stance=user_stance)

            _save_to_memory(research_id, "academic_debate", user_input, user_id,
                            summary, session.all_outputs)

            session.finished = True

        except Exception as e:
            traceback.print_exc()
            session.error = str(e)
            yield _sse("error", message=f"辩论失败: {str(e)[:200]}")
        finally:
            set_active_bus(None)
            bus.mark_done()

    def _node_to_sse(self, node_name: str, state: WorkflowState) -> Optional[dict]:
        """将 node 名称和 state 转为 SSE 事件"""
        if node_name == "__start__":
            return None
        if node_name == "__end__":
            return None

        message = f"执行节点: {node_name}"
        agent = None
        output_preview = None

        # 提取最近的 agent 输出预览
        if state.get("agent_outputs"):
            latest_key = list(state["agent_outputs"].keys())[-1] if state["agent_outputs"] else None
            if latest_key:
                agent = latest_key
                output_preview = state["agent_outputs"][latest_key][:200] + "..."

        return {
            "event": "stage",
            "data": {
                "stage": node_name,
                "status": "completed",
                "message": message,
                "agent": agent,
                "output_preview": output_preview,
            }
        }


# 全局单例
_engine: Optional[WorkflowEngine] = None

# ── 互动辩论会话管理 ──

class DebateSession:
    """一次互动辩论的会话状态 (pause/resume 机制)"""

    def __init__(self, research_id: str, user_input: str, user_id: str):
        self.research_id = research_id
        self.user_input = user_input
        self.user_id = user_id
        self.round = 0
        self.all_outputs = {}
        self.user_stance = "neutral"
        self.resume_event = asyncio.Event()
        self.user_intervention = ""
        self.finished = False
        self.error = None

    def resume(self, intervention: str = "", stance: str = ""):
        self.user_intervention = intervention
        if stance:
            self.user_stance = stance
        self.resume_event.set()
        self.resume_event.clear()


# 活跃的辩论会话
_debate_sessions: Dict[str, DebateSession] = {}


def get_debate_session(research_id: str) -> Optional[DebateSession]:
    return _debate_sessions.get(research_id)


def get_workflow_engine() -> WorkflowEngine:
    global _engine
    if _engine is None:
        _engine = WorkflowEngine()
    return _engine


def _sse(event: str, **kwargs) -> dict:
    """构造 SSE 事件"""
    import json
    return {"event": event, "data": json.dumps(kwargs, ensure_ascii=False)}


def _fmt_outputs(outputs: dict) -> str:
    """格式化已收集的 agent 输出供下游参考"""
    if not outputs:
        return "(无)"
    return "\n\n".join(f"[{k}] {v[:300]}" for k, v in outputs.items())


def _save_to_memory(research_id: str, scene_type: str, user_input: str,
                    user_id: str, output: str, agent_outputs: Dict[str, str]):
    """Workflow 完成后自动保存到长期记忆（异步、非阻塞）"""
    import threading

    def _do_save():
        try:
            from memory.long_term_memory import get_long_term_memory
            from memory.short_term_memory import save_session_to_memory, SessionRecord
            import time

            memory = get_long_term_memory()

            # 提取研究主题关键词
            topics = _extract_topics(user_input, agent_outputs)

            memory.save_research(
                user_id=user_id,
                research_id=research_id,
                scene_type=scene_type,
                query=user_input,
                output=output,
                metadata={"agent_count": len(agent_outputs), "topics": topics},
            )

            # 更新用户画像
            memory.update_profile(user_id=user_id, topics=topics)

            # ── 短期记忆: 保存会话摘要 ──
            summary = output[:500] if output else user_input[:200]
            record = SessionRecord(
                research_id=research_id,
                user_id=user_id,
                scene_type=scene_type,
                query=user_input[:200],
                summary=summary,
                topics=topics,
                timestamp=time.time(),
            )
            save_session_to_memory(record)

        except Exception:
            pass  # 记忆保存失败不应影响主流程

    t = threading.Thread(target=_do_save, daemon=True)
    t.start()


def _extract_topics(user_input: str, agent_outputs: Dict[str, str]) -> list:
    """从研究输入和 Agent 输出中提取主题关键词"""
    import re
    # 物理领域关键词库
    keywords = [
        "量子力学", "广义相对论", "标准模型", "弦理论", "凝聚态物理",
        "量子场论", "宇宙学", "量子计算", "粒子物理", "中微子",
        "超导", "拓扑", "引力波", "黑洞", "暗物质", "暗能量",
        "AdS/CFT", "全息", "纠缠", "退相干", "量子相变",
        "规范理论", "重整化", "对称性", "自发破缺", "杨-米尔斯",
    ]
    text = f"{user_input} {' '.join(agent_outputs.values())}".lower()
    found = []
    for kw in keywords:
        if any(c in text for c in [kw.lower(), kw]):
            found.append(kw)
    return found[:10] if found else [user_input[:20]]