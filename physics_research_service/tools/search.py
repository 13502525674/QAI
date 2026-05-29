"""
Tool 层 — Agent 可调用的工具集合
Phase 2: 接入 LlamaIndex RAG 检索 + SymPy 符号计算
"""
from langchain.tools import tool
from langchain.tools import StructuredTool

from rag.retriever import get_rag_retriever


# ============================================================
# 文献检索工具 (接入 RAG)
# ============================================================

@tool
def search_literature(query: str) -> str:
    """检索物理学文献数据库，返回相关文献摘要和关键信息。

    适用场景：理论推演前查找已有研究、验证假设是否已有相关文献支持。
    Args:
        query: 检索关键词或短语（支持中英文，如 "quantum gravity black hole" 或 "量子引力 黑洞"）
    Returns:
        相关文献摘要列表，含相关度评分
    """
    try:
        retriever = get_rag_retriever()
        results = retriever.query(query)
        return f"=== 文献检索结果: {query} ===\n{results}"
    except Exception as e:
        return f"文献检索出错: {e}"


@tool
def search_experiment(method_or_topic: str) -> str:
    """检索实验方法学文献，查找已有实验方案、测量方法和可行性报告。

    Args:
        method_or_topic: 实验方法名称或研究主题（如 "double-slit experiment measurement"）
    Returns:
        实验方法描述和参考信息
    """
    try:
        retriever = get_rag_retriever()
        results = retriever.query(f"实验方法 {method_or_topic}")
        return f"=== 实验方法检索: {method_or_topic} ===\n{results}"
    except Exception as e:
        return f"实验方法检索出错: {e}"


@tool
def cross_domain_search(query: str) -> str:
    """检索跨学科文献，从数学、化学、生物、工程等领域寻找可类比的方法论。

    Args:
        query: 检索关键词（建议包含目标领域名，如 "topology applied to physics"）
    Returns:
        跨学科相关文献
    """
    try:
        retriever = get_rag_retriever()
        results = retriever.query(f"跨学科 {query}")
        return f"=== 跨学科检索: {query} ===\n{results}"
    except Exception as e:
        return f"跨学科检索出错: {e}"


# ============================================================
# 符号计算工具 (SymPy)
# ============================================================

@tool
def symbolic_compute(expression: str) -> str:
    """使用 SymPy 执行符号计算：求导、积分、化简、解方程、极限等。

    支持的运算：
    - 化简: simplify(expression)
    - 求导: diff(expression, x)
    - 积分: integrate(expression, x)
    - 展开: expand(expression)
    - 因式分解: factor(expression)
    - 求解: solve(equation, x)
    - 极限: limit(expression, x, 0)
    - 矩阵特征值: eigenvalues(matrix)

    Args:
        expression: Python/SymPy 语法的数学表达式
    Returns:
        计算结果（含 LaTeX 渲染）
    """
    try:
        import sympy as sp
        from sympy import symbols, simplify, diff, integrate, limit, solve, expand, factor, Matrix
        x, y, z, t = symbols('x y z t')
        # 安全执行：仅允许 sympy 内置函数
        safe_dict = {
            'x': x, 'y': y, 'z': z, 't': t,
            'simplify': simplify, 'diff': diff, 'integrate': integrate,
            'limit': limit, 'solve': solve, 'expand': expand, 'factor': factor,
            'Matrix': Matrix, 'symbols': symbols,
            'sin': sp.sin, 'cos': sp.cos, 'tan': sp.tan,
            'exp': sp.exp, 'log': sp.log, 'sqrt': sp.sqrt,
            'pi': sp.pi, 'E': sp.E, 'oo': sp.oo,
        }
        result = eval(expression, {"__builtins__": {}}, safe_dict)
        latex_str = sp.latex(result) if hasattr(result, 'is_Matrix') or hasattr(result, 'free_symbols') else str(result)
        return f"计算: {expression}\n结果: {result}\nLaTeX: {latex_str}"
    except Exception as e:
        return f"符号计算出错: {e}\n提示: 支持 simplify(x**2+2*x+1), diff(sin(x),x), integrate(x**2,x) 等"


@tool
def dimensional_analysis(equation_desc: str) -> str:
    """验证物理公式的量纲一致性。

    Args:
        equation_desc: 物理方程描述，如 "F = m * a 力等于质量乘加速度"
    Returns:
        量纲分析结果
    """
    # 基本量纲映射
    dimensions = {
        "长度": "L", "质量": "M", "时间": "T", "电流": "I",
        "温度": "Θ", "物质的量": "N", "发光强度": "J",
    }
    return f"=== 量纲分析: {equation_desc} ===\n" + \
           f"已知量纲表: {dimensions}\n" + \
           f"(完整量纲自动推导将在 Phase 3 实现，目前请手动验证量纲一致性)"


# ============================================================
# 工具注册表
# ============================================================

ALL_TOOLS = [
    search_literature,
    search_experiment,
    cross_domain_search,
    symbolic_compute,
    dimensional_analysis,
]