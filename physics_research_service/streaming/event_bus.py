"""
StreamEventBus — Agent 思考过程的全局事件总线

核心设计:
  - asyncio.Queue 作为事件通道，Agent 向队列推送事件，Engine 从队列消费并 yield SSE
  - contextvars.ContextVar 传递当前活跃的事件总线，避免显式传参
  - 支持 ReAct Agent 的 Thought→Action→Observation 流式输出
"""
import asyncio
from contextvars import ContextVar
from typing import Dict, Any, Optional

# Agent 名称到中文标签映射
agent_label_map: Dict[str, str] = {
    "coordinator": "研究协调员",
    "theoretical_physicist": "理论物理学家",
    "experimental_physicist": "实验物理学家",
    "mathematical_methodologist": "数学方法论专家",
    "interdisciplinary_researcher": "跨学科研究者",
    "peer_reviewer": "同行评审人",
}

_current_bus: ContextVar[Optional["StreamEventBus"]] = ContextVar("stream_bus", default=None)


def get_active_bus() -> Optional["StreamEventBus"]:
    """获取当前活跃的事件总线"""
    return _current_bus.get()


def set_active_bus(bus: Optional["StreamEventBus"]):
    """设置当前活跃的事件总线"""
    _current_bus.set(bus)


async def push_agent_thought(agent_name: str, content: str):
    """推送 Agent 思考步骤"""
    bus = get_active_bus()
    if bus is None:
        return
    label = agent_label_map.get(agent_name, agent_name)
    await bus.push({
        "event": "agent_thought",
        "data": {
            "agent": agent_name,
            "agent_label": label,
            "type": "thought",
            "content": content[:500],
        },
    })


async def push_agent_action(agent_name: str, tool_name: str, tool_args: str = ""):
    """推送 Agent 工具调用"""
    bus = get_active_bus()
    if bus is None:
        return
    label = agent_label_map.get(agent_name, agent_name)
    await bus.push({
        "event": "agent_thought",
        "data": {
            "agent": agent_name,
            "agent_label": label,
            "type": "action",
            "tool": tool_name,
            "tool_args": tool_args[:300],
        },
    })


async def push_agent_observation(agent_name: str, content: str):
    """推送工具返回的观察结果"""
    bus = get_active_bus()
    if bus is None:
        return
    label = agent_label_map.get(agent_name, agent_name)
    await bus.push({
        "event": "agent_thought",
        "data": {
            "agent": agent_name,
            "agent_label": label,
            "type": "observation",
            "content": content[:600],
        },
    })


async def push_agent_output(agent_name: str, content: str):
    """推送 Agent 最终输出预览"""
    bus = get_active_bus()
    if bus is None:
        return
    label = agent_label_map.get(agent_name, agent_name)
    await bus.push({
        "event": "agent_thought",
        "data": {
            "agent": agent_name,
            "agent_label": label,
            "type": "output",
            "content": content[:300],
        },
    })


async def push_stage_event(stage: str, message: str, status: str = "running",
                           agent: str = None, output_preview: str = None):
    """推送工作流阶段事件"""
    bus = get_active_bus()
    if bus is None:
        return
    data = {
        "stage": stage,
        "status": status,
        "message": message,
    }
    if agent:
        data["agent"] = agent
    if output_preview:
        data["output_preview"] = output_preview
    await bus.push({
        "event": "stage",
        "data": data,
    })


class StreamEventBus:
    """流式事件总线 — 线程安全的 asyncio.Queue 封装"""

    def __init__(self, maxsize: int = 1024):
        self._queue: asyncio.Queue[Dict[str, Any]] = asyncio.Queue(maxsize=maxsize)
        self._done = False

    async def push(self, event: Dict[str, Any]):
        """向队列推送事件"""
        if self._done:
            return
        try:
            self._queue.put_nowait(event)
        except asyncio.QueueFull:
            pass

    async def get(self, timeout: float = 5.0) -> Optional[Dict[str, Any]]:
        """从队列获取事件，带超时"""
        try:
            return await asyncio.wait_for(self._queue.get(), timeout=timeout)
        except asyncio.TimeoutError:
            return None

    async def get_all_pending(self) -> list:
        """非阻塞获取所有待处理事件"""
        events = []
        while True:
            try:
                event = self._queue.get_nowait()
                events.append(event)
            except asyncio.QueueEmpty:
                break
        return events

    def mark_done(self):
        """标记事件总线已完成"""
        self._done = True

    @property
    def is_empty(self) -> bool:
        return self._queue.empty()