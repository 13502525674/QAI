"""
Agent Registry — 统一管理所有 Agent 实例（懒加载单例）
"""
from typing import Dict, Type, Optional

from agents.base import BaseAgent


# 延迟导入，避免循环依赖
_agent_instances: Dict[str, BaseAgent] = {}
_agent_classes: Dict[str, Type[BaseAgent]] = {}
_initialized = False


def _init_registry():
    """懒加载初始化 registry"""
    global _initialized
    if _initialized:
        return

    from agents.theoretical import TheoreticalPhysicist
    from agents.experimental import ExperimentalPhysicist
    from agents.mathematical import MathematicalMethodologist
    from agents.interdisciplinary import InterdisciplinaryResearcher
    from agents.peer_reviewer import AcademicPeerReviewer
    from agents.coordinator import ResearchCoordinator

    _agent_classes.update({
        "theoretical_physicist": TheoreticalPhysicist,
        "experimental_physicist": ExperimentalPhysicist,
        "mathematical_methodologist": MathematicalMethodologist,
        "interdisciplinary_researcher": InterdisciplinaryResearcher,
        "peer_reviewer": AcademicPeerReviewer,
        "coordinator": ResearchCoordinator,
    })
    _initialized = True


def get_agent(agent_name: str) -> Optional[BaseAgent]:
    """根据名称获取 Agent 实例（懒加载单例）"""
    _init_registry()

    if agent_name not in _agent_classes:
        return None

    if agent_name not in _agent_instances:
        _agent_instances[agent_name] = _agent_classes[agent_name]()

    return _agent_instances[agent_name]


def list_agents() -> Dict[str, str]:
    """列出所有已注册的 Agent"""
    _init_registry()

    result = {}
    for name, cls in _agent_classes.items():
        temp = cls()
        result[name] = temp.agent_label
    return result


def get_all_agent_names() -> list:
    """获取所有 Agent 名称列表"""
    _init_registry()
    return list(_agent_classes.keys())


# 便捷工厂函数
def get_theoretical() -> BaseAgent:
    return get_agent("theoretical_physicist")


def get_experimental() -> BaseAgent:
    return get_agent("experimental_physicist")


def get_mathematical() -> BaseAgent:
    return get_agent("mathematical_methodologist")


def get_interdisciplinary() -> BaseAgent:
    return get_agent("interdisciplinary_researcher")


def get_peer_reviewer() -> BaseAgent:
    return get_agent("peer_reviewer")


def get_coordinator() -> BaseAgent:
    return get_agent("coordinator")