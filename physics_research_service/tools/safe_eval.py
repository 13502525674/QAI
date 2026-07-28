"""
SymPy AST 安全沙箱 — P0-5 优化

基于 AST 解析的安全求值器，替换 eval() + __builtins__ 禁用方案。

显式拒绝所有危险节点：
- Attribute (obj.attr)
- Subscript (obj[key])
- Lambda
- Import / ImportFrom
- GeneratorExp / ListComp / SetComp / DictComp
- Starred / NamedExpr / Assign / Delete
- Global / Nonlocal / Yield / Await

白名单允许：
- 数字常量 (int/float/complex)
- 标识符 (x, y, z, t, pi, E, oo, i)
- 二元运算 (+, -, *, /, **, %)
- 一元运算 (-, +)
- 函数调用 (白名单函数: sin, cos, diff, integrate, ...)
- 元组 (用于 multi-arg 函数)

攻击向量测试：
- x.__class__ -> ValueError: 禁止的语法: Attribute
- x[0] -> ValueError: 禁止的语法: Subscript
- (lambda: 1)() -> ValueError: 禁止的语法: Lambda
- __import__('os') -> ValueError: 非法标识符: __import__
- [x for x in range(10)] -> ValueError: 禁止的语法: ListComp
"""
import ast
import operator
import logging
import sympy as sp

logger = logging.getLogger(__name__)


class SafeMathEvaluator:
    """基于 AST 的数学表达式安全求值器"""

    SAFE_OPERATORS = {
        ast.Add: operator.add,
        ast.Sub: operator.sub,
        ast.Mult: operator.mul,
        ast.Div: operator.truediv,
        ast.Pow: operator.pow,
        ast.Mod: operator.mod,
        ast.USub: operator.neg,
        ast.UAdd: operator.pos,
    }

    SAFE_FUNCTIONS = {
        # 三角函数
        "sin": sp.sin, "cos": sp.cos, "tan": sp.tan,
        "asin": sp.asin, "acos": sp.acos, "atan": sp.atan,
        "sinh": sp.sinh, "cosh": sp.cosh, "tanh": sp.tanh,
        # 指数对数
        "exp": sp.exp, "log": sp.log, "sqrt": sp.sqrt,
        # SymPy 操作
        "simplify": sp.simplify, "expand": sp.expand, "factor": sp.factor,
        "diff": sp.diff, "integrate": sp.integrate,
        "limit": sp.limit, "solve": sp.solve,
        "series": sp.series, "apart": sp.apart, "together": sp.together,
        # 矩阵
        "Matrix": sp.Matrix, "eye": sp.eye, "zeros": sp.zeros, "ones": sp.ones,
        "det": sp.det, "transpose": sp.transpose,
        # 其他
        "symbols": sp.symbols, "Symbol": sp.Symbol,
        "Rational": sp.Rational, "Float": sp.Float,
        "Abs": sp.Abs, "re": sp.re, "im": sp.im, "conjugate": sp.conjugate,
        # 常量
        "oo": sp.oo, "pi": sp.pi, "E": sp.E, "I": sp.I,
    }

    SAFE_NAMES = {"x", "y", "z", "t", "r", "theta", "phi", "n", "k", "m",
                  "pi", "E", "oo", "I", "i"}

    # 显式拒绝的节点类型
    FORBIDDEN_NODES = (
        ast.Attribute,       # 禁止 obj.attr
        ast.Subscript,       # 禁止 obj[key]
        ast.Lambda,          # 禁止 lambda
        ast.Import,          # 禁止 import
        ast.ImportFrom,      # 禁止 from ... import
        ast.GeneratorExp,    # 禁止 (x for x in ...)
        ast.ListComp,        # 禁止 [x for x in ...]
        ast.SetComp,         # 禁止 {x for x in ...}
        ast.DictComp,        # 禁止 {k: v for ...}
        ast.Starred,         # 禁止 *args
        ast.NamedExpr,       # 禁止 :=
        ast.Delete,          # 禁止 del
        ast.Assign,          # 禁止 =
        ast.AugAssign,       # 禁止 +=
        ast.AnnAssign,       # 禁止 :=
        ast.Global,          # 禁止 global
        ast.Nonlocal,        # 禁止 nonlocal
        ast.Yield,           # 禁止 yield
        ast.YieldFrom,       # 禁止 yield from
        ast.Await,           # 禁止 await
        ast.IfExp,           # 禁止三元表达式（防复杂逻辑）
    )

    def evaluate(self, expression: str):
        """安全求值

        Args:
            expression: 数学表达式字符串

        Returns:
            求值结果

        Raises:
            ValueError: 表达式包含禁止的语法
        """
        try:
            tree = ast.parse(expression, mode="eval")
        except SyntaxError as e:
            raise ValueError(f"语法错误: {e}")

        return self._eval_node(tree.body)

    def _eval_node(self, node):
        """递归求值 AST 节点"""

        # 显式拒绝危险节点
        if isinstance(node, self.FORBIDDEN_NODES):
            raise ValueError(f"禁止的语法: {type(node).__name__}")

        # 常量
        if isinstance(node, ast.Constant):
            if isinstance(node.value, (int, float, complex)):
                return node.value
            raise ValueError(f"非法常量类型: {type(node.value).__name__}")

        # 标识符
        elif isinstance(node, ast.Name):
            # 拒绝双下划线名称
            if node.id.startswith("__"):
                raise ValueError(f"非法标识符: {node.id}")

            if node.id in self.SAFE_FUNCTIONS:
                return self.SAFE_FUNCTIONS[node.id]
            if node.id in self.SAFE_NAMES:
                if node.id in ("x", "y", "z", "t", "r", "theta", "phi", "n", "k", "m"):
                    return sp.Symbol(node.id)
                return self.SAFE_FUNCTIONS.get(node.id, sp.Symbol(node.id))
            raise ValueError(f"非法标识符: {node.id}")

        # 二元运算
        elif isinstance(node, ast.BinOp):
            op_type = type(node.op)
            if op_type not in self.SAFE_OPERATORS:
                raise ValueError(f"非法运算符: {op_type.__name__}")
            left = self._eval_node(node.left)
            right = self._eval_node(node.right)
            return self.SAFE_OPERATORS[op_type](left, right)

        # 一元运算
        elif isinstance(node, ast.UnaryOp):
            op_type = type(node.op)
            if op_type not in self.SAFE_OPERATORS:
                raise ValueError(f"非法一元运算: {op_type.__name__}")
            operand = self._eval_node(node.operand)
            return self.SAFE_OPERATORS[op_type](operand)

        # 函数调用（严格限制）
        elif isinstance(node, ast.Call):
            # 只允许直接函数调用，禁止 obj.method()
            if not isinstance(node.func, ast.Name):
                raise ValueError("只允许直接函数调用，禁止 obj.method() 形式")

            func_name = node.func.id
            if func_name not in self.SAFE_FUNCTIONS:
                raise ValueError(f"非法函数: {func_name}")

            func = self.SAFE_FUNCTIONS[func_name]
            args = [self._eval_node(arg) for arg in node.args]

            # 禁止关键字参数
            if node.keywords:
                raise ValueError("禁止使用关键字参数")

            return func(*args)

        # 元组（用于 multi-arg 函数如 diff(f, x)）
        elif isinstance(node, ast.Tuple):
            return tuple(self._eval_node(e) for e in node.elts)

        # 列表（用于矩阵构造 Matrix([[1,2],[3,4]])）
        elif isinstance(node, ast.List):
            return [self._eval_node(e) for e in node.elts]

        else:
            raise ValueError(f"不支持的语法节点: {type(node).__name__}")


# 全局单例
_evaluator = SafeMathEvaluator()


def safe_symbolic_compute(expression: str) -> str:
    """安全的符号计算入口

    替换 tools/search.py 中的 eval() 调用。

    Args:
        expression: 数学表达式

    Returns:
        计算结果（含 LaTeX）
    """
    try:
        result = _evaluator.evaluate(expression)

        # 生成 LaTeX
        if hasattr(result, "is_Matrix") or hasattr(result, "free_symbols"):
            latex_str = sp.latex(result)
        else:
            latex_str = str(result)

        return f"计算: {expression}\n结果: {result}\nLaTeX: {latex_str}"

    except ValueError as e:
        logger.warning(f"[SafeEval] 拦截: {e}, expr={expression[:50]}")
        return (
            f"安全拦截: {e}\n"
            f"提示: 支持 simplify(x**2+2*x+1), diff(sin(x),x), "
            f"integrate(x**2,x), Matrix([[1,2],[3,4]]) 等"
        )
    except Exception as e:
        logger.error(f"[SafeEval] 求值失败: {e}, expr={expression[:50]}")
        return f"计算失败: {e}"
