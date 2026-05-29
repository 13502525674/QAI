"""
MCP 工具注册中心 — 实现工具即插即用 (Model Context Protocol)

功能:
  - 统一工具注册/注销
  - 按标签 (tag) 检索工具 (Agent 按角色获取)
  - 运行时热加载 (register/unregister)
"""
from typing import List, Optional, Callable, Set
from dataclasses import dataclass, field
from langchain.tools import BaseTool


@dataclass
class ToolMeta:
    """工具元数据"""
    name: str
    description: str
    tool: BaseTool
    tags: Set[str] = field(default_factory=set)    # 功能标签
    agents: Set[str] = field(default_factory=set)   # 可用 Agent


class ToolRegistry:
    """MCP 工具注册中心 — 单例模式"""

    _instance: Optional["ToolRegistry"] = None

    def __init__(self):
        self._tools: dict[str, BaseTool] = {}
        self._meta: dict[str, ToolMeta] = {}

    @classmethod
    def get_instance(cls) -> "ToolRegistry":
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance

    # ── 注册/注销 ──

    def register(
        self,
        tool: BaseTool,
        tags: Optional[List[str]] = None,
        agents: Optional[List[str]] = None,
    ) -> None:
        """注册工具到注册中心"""
        name = tool.name
        meta = ToolMeta(
            name=name,
            description=tool.description or "",
            tool=tool,
            tags=set(tags or []),
            agents=set(agents or []),
        )
        self._tools[name] = tool
        self._meta[name] = meta

    def unregister(self, name: str) -> bool:
        """注销工具，返回是否成功"""
        if name in self._tools:
            del self._tools[name]
            del self._meta[name]
            return True
        return False

    # ── 查询 ──

    def get(self, name: str) -> Optional[BaseTool]:
        """按名称获取工具"""
        return self._tools.get(name)

    def get_by_tag(self, tag: str) -> List[BaseTool]:
        """按标签获取工具"""
        return [m.tool for m in self._meta.values() if tag in m.tags]

    def get_by_agent(self, agent_name: str) -> List[BaseTool]:
        """获取某 Agent 可用的所有工具"""
        return [m.tool for m in self._meta.values()
                if not m.agents or agent_name in m.agents]

    def get_by_tags(self, tags: List[str], match_all: bool = False) -> List[BaseTool]:
        """按多个标签获取工具。match_all=True 时要求同时满足全部标签"""
        result = []
        for m in self._meta.values():
            if match_all:
                if all(t in m.tags for t in tags):
                    result.append(m.tool)
            else:
                if any(t in m.tags for t in tags):
                    result.append(m.tool)
        return result

    # ── 全量查询 ──

    def list_all(self) -> List[BaseTool]:
        """列出全部已注册工具"""
        return list(self._tools.values())

    def list_names(self) -> List[str]:
        """列出全部工具名称"""
        return list(self._tools.keys())

    def tool_count(self) -> int:
        """已注册工具数"""
        return len(self._tools)


def get_tool_registry() -> ToolRegistry:
    """获取工具注册中心单例"""
    return ToolRegistry.get_instance()


# ═══════════════════════════════════════════════════════════
# 初始化：注册所有内置工具
# ═══════════════════════════════════════════════════════════

def init_builtin_tools():
    """初始化所有内置工具到注册中心"""
    from tools.search import (
        search_literature,
        search_experiment,
        cross_domain_search,
        symbolic_compute,
        dimensional_analysis,
    )
    registry = get_tool_registry()

    # 文献检索
    registry.register(
        search_literature,
        tags=["search", "literature", "theory"],
        agents=["theoretical_physicist", "experimental_physicist",
                "interdisciplinary_researcher", "peer_reviewer"],
    )
    # 实验检索
    registry.register(
        search_experiment,
        tags=["search", "experiment", "method"],
        agents=["experimental_physicist", "theoretical_physicist"],
    )
    # 跨学科检索
    registry.register(
        cross_domain_search,
        tags=["search", "cross_domain", "innovation"],
        agents=["interdisciplinary_researcher", "theoretical_physicist"],
    )
    # 符号计算
    registry.register(
        symbolic_compute,
        tags=["compute", "math", "derivation"],
        agents=["mathematical_methodologist"],
    )
    # 量纲分析
    registry.register(
        dimensional_analysis,
        tags=["compute", "verify", "units"],
        agents=["mathematical_methodologist", "experimental_physicist"],
    )