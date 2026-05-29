"""
物理学术研究交流平台 — FastAPI 主入口 (Phase 2)
统一使用 deepseek-v4-flash，单 Agent 端到端可用

启动方式:
    python main.py
"""
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent
sys.path.insert(0, str(PROJECT_ROOT))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from config.settings import get_settings
from api.routes import router as research_router


def create_app() -> FastAPI:
    settings = get_settings()

    app = FastAPI(
        title="物理学术研究交流平台",
        description="Multi-Agent 物理学术研究服务 (Phase 2 — deepseek-v4-flash)",
        version="0.2.0",
        docs_url="/api/research/docs" if settings.debug else None,
        redoc_url=None,
    )

    # CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(research_router)

    @app.on_event("startup")
    async def startup_event():
        print("=" * 60)
        print("  物理学术研究交流平台 — Phase 2")
        print(f"  模型: {settings.dashscope_model_name}")
        print(f"  端点: {settings.dashscope_base_url}")
        print("=" * 60)

        # 初始化 RAG 向量库 + 种子数据
        try:
            from rag.retriever import get_rag_retriever, SEED_DOCUMENTS
            retriever = get_rag_retriever()
            # 检查是否已有数据，无则加载种子文档
            existing = retriever.count()
            if existing == 0:
                retriever.add_documents(SEED_DOCUMENTS)
                print(f"[Startup] 已加载 {len(SEED_DOCUMENTS)} 篇种子文档到向量库")
            else:
                print(f"[Startup] 向量库已有 {existing} 条记录，跳过种子加载")
        except Exception as e:
            print(f"[Startup] RAG 初始化警告 (非致命): {e}")

        # 验证 LLM 连通性
        try:
            from agents.theoretical import create_theoretical_physicist
            agent = create_theoretical_physicist()
            test_resp = await agent.llm.ainvoke("ping")
            print(f"[Startup] LLM 连通性 OK ({settings.dashscope_model_name})")
        except Exception as e:
            print(f"[Startup] LLM 连通性警告 (非致命): {e}")

    @app.on_event("shutdown")
    async def shutdown_event():
        print("[Shutdown] 物理学术研究服务已停止")

    return app


app = create_app()

if __name__ == "__main__":
    import uvicorn
    settings = get_settings()
    uvicorn.run(
        "main:app",
        host=settings.service_host,
        port=settings.service_port,
        reload=settings.debug,
        log_level="info",
    )