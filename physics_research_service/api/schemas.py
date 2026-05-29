"""
Pydantic 请求/响应模型
"""
from typing import Optional, Literal
from datetime import datetime
from pydantic import BaseModel, Field


# ── 请求模型 ──

class ResearchRequest(BaseModel):
    """启动研究的请求体"""
    scene_type: Literal[
        "literature_review",
        "research_proposal",
        "paper_review",
        "academic_debate",
        "custom_workflow",
    ] = Field(..., description="研究场景类型")
    user_input: str = Field(..., min_length=10, description="研究主题或论文内容")
    options: Optional[dict] = Field(default_factory=dict, description="场景专用选项")
    fast_mode: bool = Field(default=False, description="快速模式：跳过评审修订轮")


class DebateInterjection(BaseModel):
    """用户在辩论中插入论点"""
    research_id: str
    content: str
    position: Literal["pro", "con", "neutral"] = "neutral"


class CustomWorkflowRequest(BaseModel):
    """自定义 Workflow 请求"""
    workflow_definition: dict = Field(..., description="节点和边的定义")


# ── 响应模型 ──

class SSEEvent(BaseModel):
    """SSE 事件格式"""
    event: str = Field(..., description="事件类型: stage / agent / progress / done / error")
    stage: Optional[str] = None
    agent: Optional[str] = None
    status: Optional[str] = None   # running / completed / failed
    message: Optional[str] = None
    data: Optional[dict] = None


class ResearchHistoryItem(BaseModel):
    """研究历史记录"""
    research_id: str
    scene_type: str
    user_input: str
    created_at: datetime
    duration_seconds: Optional[int] = None


class HealthResponse(BaseModel):
    """健康检查"""
    status: str = "ok"
    service: str = "physics-research-service"
    version: str = "0.1.0"