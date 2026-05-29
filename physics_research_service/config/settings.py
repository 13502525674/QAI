"""
全局配置管理 — 从环境变量 aliQwen-api 读取百炼 Key，统一使用 deepseek-v4-flash
"""
import os
from functools import lru_cache
from pydantic_settings import BaseSettings
from pathlib import Path


class Settings(BaseSettings):
    """应用全局配置"""

    # --- 百炼 / DeepSeek-V4-Pro ---
    dashscope_api_key: str = ""
    dashscope_base_url: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    dashscope_model_name: str = "deepseek-v4-flash"

    # --- Agent 模型 (Phase 2 全部统一为 deepseek-v4-Flash) ---
    agent_model_default: str = "deepseek-v4-flash"
    # 开发/测试模式 — True 时所有 Agent.execute() 不走 LLM，返回 mock 结果
    mock_mode: bool = False
    # 便宜模型用于测试 (不消耗 deepseek token)
    test_model_name: str = "qwen-turbo"

    # --- Redis ---
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = ""
    redis_db: int = 0

    # --- Chroma (已废弃, 改用 pgvector) ---
    chroma_persist_dir: str = "./chroma_data"

    # --- PostgreSQL / pgvector ---
    pg_host: str = "localhost"
    pg_port: int = 5432
    pg_user: str = "postgres"
    pg_password: str = ""
    pg_database: str = "physics_rag"

    # --- LangSmith (可选) ---
    langchain_tracing_v2: bool = False
    langchain_api_key: str = ""
    langchain_project: str = "physics-research"

    # --- 服务 ---
    service_host: str = "0.0.0.0"
    service_port: int = 8000
    debug: bool = True

    # --- 通用 ---
    project_root: Path = Path(__file__).parent.parent

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

    @property
    def redis_url(self) -> str:
        """构建 Redis 连接 URL"""
        if self.redis_password:
            return f"redis://:{self.redis_password}@{self.redis_host}:{self.redis_port}/{self.redis_db}"
        return f"redis://{self.redis_host}:{self.redis_port}/{self.redis_db}"

    @property
    def prompts_dir(self) -> Path:
        """System Prompt 模板目录"""
        return self.project_root / "config" / "prompts"


@lru_cache()
def get_settings() -> Settings:
    """获取配置单例，自动合并环境变量 aliQwen-api"""
    s = Settings()
    # 从系统环境变量 aliQwen-api 读取 API Key（优先于 .env）
    env_key = os.environ.get("aliQwen-api", "")
    if env_key and not s.dashscope_api_key:
        s.dashscope_api_key = env_key
    return s