# tools package — MCP 工具注册中心自动初始化
from tools.registry import init_builtin_tools, get_tool_registry

# 模块加载时自动注册所有内置工具
init_builtin_tools()