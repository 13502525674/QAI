"""
API 路由 — Phase 4: Workflow 引擎
"""
import asyncio
import json
import traceback
import uuid
from typing import Optional, AsyncGenerator
from datetime import datetime

from fastapi import APIRouter, Request, HTTPException, Depends, Query, Body
from fastapi.responses import StreamingResponse
from sse_starlette.sse import EventSourceResponse

from api.middleware import jwt_auth_middleware
from api.schemas import (
    ResearchRequest,
    ResearchHistoryItem,
    HealthResponse,
)
from agents import (
    get_agent,
    list_agents,
    get_theoretical,
    get_experimental,
    get_mathematical,
    get_interdisciplinary,
    get_peer_reviewer,
    get_coordinator,
)
from workflows import get_workflow_engine, SceneType

router = APIRouter(prefix="/api/research", tags=["学术研究"])


# ── 健康检查 ──

@router.get("/health", response_model=HealthResponse)
async def health_check():
    return HealthResponse()


# ── Agent 列表 ──

@router.get("/agents")
async def get_agent_list():
    """获取所有已注册的 Agent 列表及能力描述"""
    agents = list_agents()
    return {
        "count": len(agents),
        "agents": agents,
        "default_model": "deepseek-v4-flash",
    }


# ── 通用 Agent 测试端点 ──

@router.post("/agent/{agent_name}/test")
async def test_agent(
    agent_name: str,
    request: Request,
    body: ResearchRequest,
    user: dict = Depends(jwt_auth_middleware),
):
    """通用 Agent 测试端点（非流式）"""
    agent = get_agent(agent_name)
    if agent is None:
        raise HTTPException(status_code=404, detail=f"Agent '{agent_name}' 不存在。可用: {list(list_agents().keys())}")

    try:
        context = body.options or {}
        result = await agent.execute(task=body.user_input, context=context)
        return {
            "status": "ok",
            "agent": agent_name,
            "agent_label": agent.agent_label,
            "result": result,
        }
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Agent 执行失败: {str(e)}")


# ── Coordinator 场景分析 ──

@router.post("/coordinator/analyze")
async def coordinator_analyze(
    request: Request,
    body: ResearchRequest,
    user: dict = Depends(jwt_auth_middleware),
):
    """协调员场景识别 + 任务分解（不执行）"""
    coordinator = get_coordinator()
    scene_info = await coordinator.analyze_scene(body.user_input)
    sub_tasks = await coordinator.decompose_task(
        body.user_input, scene_info.get("scene", "literature_review")
    )

    return {
        "scene": scene_info,
        "plan": sub_tasks,
    }


# ── Workflow 列表 ──

@router.get("/workflows")
async def get_workflow_list():
    """获取所有已注册的 Workflow 模板及描述"""
    engine = get_workflow_engine()
    workflows = engine.list_workflows()
    return {
        "count": len(workflows),
        "workflows": {
            k: {"description": v}
            for k, v in workflows.items()
        },
    }


# ── 文献综述 (SSE 流式, Workflow 引擎) ──

@router.post("/literature-review/stream")
async def literature_review_stream(
    request: Request,
    body: ResearchRequest,
    user: dict = Depends(jwt_auth_middleware),
):
    """SSE 流式文献综述 — Workflow A 引擎驱动"""
    engine = get_workflow_engine()
    fast_mode = body.options.get("fast_mode", False) if body.options else False

    async def event_stream():
        async for event in engine.execute_stream(
            scene_type=SceneType.LITERATURE_REVIEW,
            user_input=body.user_input,
            user_id=user.get("user_id", "anonymous") if user else "anonymous",
            fast_mode=fast_mode,
        ):
            yield _sse(event["event"], **event["data"])
    return EventSourceResponse(event_stream())


# ── 研究方案设计 (SSE 流式, Workflow 引擎) ──

@router.post("/research-proposal/stream")
async def research_proposal_stream(
    request: Request,
    body: ResearchRequest,
    user: dict = Depends(jwt_auth_middleware),
):
    """SSE 流式研究方案设计 — Workflow B 引擎驱动"""
    engine = get_workflow_engine()
    fast_mode = body.options.get("fast_mode", False) if body.options else False

    async def event_stream():
        async for event in engine.execute_stream(
            scene_type=SceneType.RESEARCH_PROPOSAL,
            user_input=body.user_input,
            user_id=user.get("user_id", "anonymous") if user else "anonymous",
            fast_mode=fast_mode,
        ):
            yield _sse(event["event"], **event["data"])
    return EventSourceResponse(event_stream())


# ── 论文预审 (SSE 流式, Workflow 引擎) ──

@router.post("/paper-review/stream")
async def paper_review_stream(
    request: Request,
    body: ResearchRequest,
    user: dict = Depends(jwt_auth_middleware),
):
    """SSE 流式论文预审 — Workflow C 引擎驱动"""
    engine = get_workflow_engine()
    fast_mode = body.options.get("fast_mode", False) if body.options else False

    async def event_stream():
        async for event in engine.execute_stream(
            scene_type=SceneType.PAPER_REVIEW,
            user_input=body.user_input,
            user_id=user.get("user_id", "anonymous") if user else "anonymous",
            fast_mode=fast_mode,
        ):
            yield _sse(event["event"], **event["data"])
    return EventSourceResponse(event_stream())


# ── 学术辩论 (SSE 流式, Workflow 引擎) ──

@router.post("/academic-debate/stream")
async def academic_debate_stream(
    request: Request,
    body: ResearchRequest,
    user: dict = Depends(jwt_auth_middleware),
):
    """SSE 流式学术辩论 — Workflow D 引擎驱动"""
    engine = get_workflow_engine()
    fast_mode = body.options.get("fast_mode", False) if body.options else False

    async def event_stream():
        async for event in engine.execute_stream(
            scene_type=SceneType.ACADEMIC_DEBATE,
            user_input=body.user_input,
            user_id=user.get("user_id", "anonymous") if user else "anonymous",
            fast_mode=fast_mode,
        ):
            yield _sse(event["event"], **event["data"])
    return EventSourceResponse(event_stream())


# ── 长期记忆 API ──

@router.get("/history")
async def get_history(
    user: dict = Depends(jwt_auth_middleware),
    scene_type: Optional[str] = Query(None),
    limit: int = Query(default=20, le=200),
):
    """获取研究历史记录（按时间倒序）"""
    from memory.long_term_memory import get_long_term_memory
    memory = get_long_term_memory()
    records = memory.get_recent_history(limit=limit)
    return {
        "count": len(records),
        "total": memory.get_history_count(),
        "items": records,
    }


@router.get("/memory/search")
async def search_memory(
    q: str = Query(..., description="搜索查询"),
    top_k: int = Query(default=5, le=20),
    user: dict = Depends(jwt_auth_middleware),
):
    """语义检索历史研究记录"""
    from memory.long_term_memory import get_long_term_memory
    memory = get_long_term_memory()
    results = memory.search_history(query=q, top_k=top_k)
    return {"query": q, "count": len(results), "results": results}


@router.get("/memory/profile")
async def get_user_profile(
    user: dict = Depends(jwt_auth_middleware),
):
    """获取当前用户的知识画像"""
    from memory.long_term_memory import get_long_term_memory
    memory = get_long_term_memory()
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    profile = memory.get_profile(user_id)
    return {"user_id": user_id, "profile": profile}


@router.get("/memory/similar-users")
async def find_similar_users(
    top_k: int = Query(default=3, le=10),
    user: dict = Depends(jwt_auth_middleware),
):
    """查找知识画像相似的用户"""
    from memory.long_term_memory import get_long_term_memory
    memory = get_long_term_memory()
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    results = memory.find_similar_profiles(user_id, top_k)
    return {"user_id": user_id, "count": len(results), "results": results}


# ── 工作流编排 API ──

_CUSTOM_WORKFLOWS = {}  # 内存存储

@router.post("/workflows/save")
async def save_workflow(
    name: str = Body(...),
    config: dict = Body(...),
    user: dict = Depends(jwt_auth_middleware),
):
    """保存自定义编排的工作流"""
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    key = f"{user_id}:{name}"
    entry = {
        "name": name,
        "config": config,
        "saved_at": datetime.utcnow().isoformat(),
    }
    _CUSTOM_WORKFLOWS[key] = entry
    return {"success": True, "workflow": entry}


@router.get("/workflows/list")
async def list_workflows(
    user: dict = Depends(jwt_auth_middleware),
):
    """获取当前用户保存的所有工作流"""
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    prefix = f"{user_id}:"
    workflows = [
        {"id": k.replace(prefix, ""), **v}
        for k, v in _CUSTOM_WORKFLOWS.items()
        if k.startswith(prefix)
    ]
    return {"user_id": user_id, "workflows": workflows}


@router.post("/workflows/execute")
async def execute_workflow(
    workflow_name: str = Body(...),
    user_input: str = Body(...),
    fast_mode: bool = Body(default=True),
    user: dict = Depends(jwt_auth_middleware),
):
    """执行自定义编排的工作流"""
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    key = f"{user_id}:{workflow_name}"
    if key not in _CUSTOM_WORKFLOWS:
        raise HTTPException(status_code=404, detail="工作流未找到")

    from workflows.engine import get_workflow_engine
    engine = get_workflow_engine()
    research_id = uuid.uuid4().hex[:12]

    async def generate():
        async for sse_event in engine.execute_custom_stream(
            research_id=research_id,
            user_input=user_input,
            user_id=user_id,
            config=_CUSTOM_WORKFLOWS[key]["config"],
            fast_mode=fast_mode,
        ):
            yield sse_event

    return StreamingResponse(generate(), media_type="text/event-stream")


# ── 互动辩论 API ──

@router.post("/debate/start")
async def start_interactive_debate(req: ResearchRequest,
                                   user: dict = Depends(jwt_auth_middleware)):
    """启动互动式学术辩论 (SSE 流，含暂停点)"""
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    from workflows.engine import get_workflow_engine

    engine = get_workflow_engine()
    research_id = uuid.uuid4().hex[:12]

    async def generate():
        async for sse_event in engine.execute_interactive_debate(
            research_id=research_id,
            user_input=req.user_input,
            user_id=user_id,
        ):
            evt = sse_event.get("event", "")
            data = sse_event.get("data", "")
            yield {"event": evt, "data": data}

    return EventSourceResponse(generate())


@router.post("/debate/continue")
async def continue_debate(
    research_id: str = Body(...),
    intervention: str = Body(default=""),
    stance: str = Body(default=""),
    user: dict = Depends(jwt_auth_middleware),
):
    """恢复辩论: 提交用户干预内容后继续下一轮"""
    from workflows.engine import get_debate_session
    session = get_debate_session(research_id)
    if not session:
        raise HTTPException(status_code=404, detail="辩论会话不存在或已过期")
    if session.finished:
        raise HTTPException(status_code=400, detail="辩论已结束")
    session.resume(intervention=intervention, stance=stance)
    return {"success": True, "research_id": research_id, "round": session.round + 1}


# ── 短期记忆 API ──

@router.get("/memory/sessions")
async def get_recent_sessions(
    limit: int = Query(default=5, le=20),
    user: dict = Depends(jwt_auth_middleware),
):
    """获取用户最近会话摘要 (短期记忆)"""
    from memory.short_term_memory import get_short_term_memory
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    stm = get_short_term_memory()
    sessions = stm.get_recent(user_id, limit)
    return {
        "user_id": user_id,
        "count": len(sessions),
        "backend": "redis" if stm.available else "memory",
        "sessions": [
            {
                "research_id": s.research_id,
                "scene_type": s.scene_type,
                "query": s.query,
                "summary": s.summary,
                "topics": s.topics,
                "timestamp": s.timestamp,
            }
            for s in sessions
        ],
    }


@router.get("/memory/context")
async def get_session_context(
    max_sessions: int = Query(default=3, le=10),
    user: dict = Depends(jwt_auth_middleware),
):
    """获取用户的会话上下文文本 (用于注入新研究)"""
    from memory.short_term_memory import get_user_context
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    ctx = get_user_context(user_id, max_sessions)
    return {"user_id": user_id, "context": ctx, "has_context": bool(ctx)}


@router.delete("/memory/sessions/{research_id}")
async def delete_session(
    research_id: str,
    user: dict = Depends(jwt_auth_middleware),
):
    """删除指定会话"""
    from memory.short_term_memory import get_short_term_memory
    user_id = user.get("user_id", "anonymous") if user else "anonymous"
    get_short_term_memory().delete_session(user_id, research_id)
    return {"success": True, "research_id": research_id}


# ── 辅助 ──

def _sse(event: str, **kwargs) -> dict:
    """构造 SSE 事件，将 event 类型嵌入 JSON data 供前端解析"""
    return {"event": event, "data": json.dumps({"event": event, **kwargs}, ensure_ascii=False)}