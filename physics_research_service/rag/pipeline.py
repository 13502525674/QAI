"""
文档清洗 Pipeline — P1-7 优化

支持 PDF/Word/Markdown/HTML/TXT 解析，包含：
- 页眉页脚去除
- SimHash 段落去重
- AST-aware 分块（保持公式/代码块完整）
- 元数据标注

使用方法:
    python -m rag.pipeline --ingest path/to/document.pdf
    python -m rag.pipeline --ingest-dir path/to/documents/
"""
import re
import json
import hashlib
import logging
import argparse
from pathlib import Path
from typing import List, Dict, Optional, Tuple
from datetime import datetime

logger = logging.getLogger(__name__)


# ============================================================
# 格式解析器
# ============================================================

class PDFParser:
    """PDF 解析 — 保留表格结构"""

    def parse(self, file_path: str) -> Dict:
        import pdfplumber
        text_parts = []
        tables = []
        with pdfplumber.open(file_path) as pdf:
            for page in pdf.pages:
                # 提取文本
                page_text = page.extract_text() or ""
                text_parts.append(page_text)
                # 提取表格
                for table in page.extract_tables():
                    tables.append(self._table_to_markdown(table))

        full_text = "\n\n".join(text_parts)
        if tables:
            full_text += "\n\n## 表格\n\n" + "\n\n".join(tables)

        return {"text": full_text, "tables": tables, "page_count": len(pdf.pages)}

    def _table_to_markdown(self, table: List[List]) -> str:
        if not table or not table[0]:
            return ""
        header = "| " + " | ".join(str(c or "") for c in table[0]) + " |\n"
        sep = "| " + " | ".join("---" for _ in table[0]) + " |\n"
        rows = "".join(
            "| " + " | ".join(str(c or "") for c in row) + " |\n"
            for row in table[1:]
        )
        return header + sep + rows


class MarkdownParser:
    """Markdown 解析 — 直接读取"""

    def parse(self, file_path: str) -> Dict:
        text = Path(file_path).read_text(encoding="utf-8")
        return {"text": text, "tables": [], "page_count": 1}


class TextParser:
    """纯文本解析"""

    def parse(self, file_path: str) -> Dict:
        text = Path(file_path).read_text(encoding="utf-8")
        return {"text": text, "tables": [], "page_count": 1}


class DocxParser:
    """Word 文档解析"""

    def parse(self, file_path: str) -> Dict:
        try:
            from docx import Document
            doc = Document(file_path)
            paragraphs = [p.text for p in doc.paragraphs if p.text.strip()]
            # 提取表格
            tables = []
            for table in doc.tables:
                rows = []
                for row in table.rows:
                    rows.append([cell.text for cell in row.cells])
                if rows:
                    tables.append(self._table_to_markdown(rows))

            full_text = "\n\n".join(paragraphs)
            if tables:
                full_text += "\n\n## 表格\n\n" + "\n\n".join(tables)

            return {"text": full_text, "tables": tables, "page_count": 1}
        except ImportError:
            logger.warning("python-docx 未安装，用 Tika 回退")
            return self._tika_fallback(file_path)

    def _table_to_markdown(self, table: List[List]) -> str:
        if not table or not table[0]:
            return ""
        header = "| " + " | ".join(str(c or "") for c in table[0]) + " |\n"
        sep = "| " + " | ".join("---" for _ in table[0]) + " |\n"
        rows = "".join(
            "| " + " | ".join(str(c or "") for c in row) + " |\n"
            for row in table[1:]
        )
        return header + sep + rows

    def _tika_fallback(self, file_path: str) -> Dict:
        """用 Apache Tika 回退解析"""
        try:
            from tika import parser as tika_parser
            raw = tika_parser.from_file(file_path)
            return {"text": raw.get("content", ""), "tables": [], "page_count": 1}
        except ImportError:
            return {"text": "", "tables": [], "page_count": 1, "error": "无可用解析器"}


PARSER_REGISTRY = {
    ".pdf": PDFParser(),
    ".md": MarkdownParser(),
    ".markdown": MarkdownParser(),
    ".txt": TextParser(),
    ".docx": DocxParser(),
}


# ============================================================
# 清洗器
# ============================================================

class HeaderFooterCleaner:
    """去除页眉页脚（简单版：去除重复出现的短行）"""

    def clean(self, doc: Dict) -> Dict:
        lines = doc["text"].split("\n")
        # 统计短行出现频率
        line_freq = {}
        for line in lines:
            line = line.strip()
            if 0 < len(line) < 50:
                line_freq[line] = line_freq.get(line, 0) + 1

        # 出现 3 次以上的短行视为页眉页脚
        boilerplate = {l for l, c in line_freq.items() if c >= 3}

        cleaned_lines = [l for l in lines if l.strip() not in boilerplate]
        doc["text"] = "\n".join(cleaned_lines)
        return doc


class DuplicateParagraphCleaner:
    """基于 SimHash 的段落去重"""

    def __init__(self, threshold: float = 0.9):
        self.threshold = threshold
        self.seen_hashes: List[int] = []

    def clean(self, doc: Dict) -> Dict:
        paragraphs = doc["text"].split("\n\n")
        unique = []
        removed = 0
        for p in paragraphs:
            p = p.strip()
            if not p:
                continue
            h = self._simhash(p)
            is_dup = any(
                self._hamming_similarity(h, s) > self.threshold
                for s in self.seen_hashes
            )
            if is_dup:
                removed += 1
            else:
                unique.append(p)
                self.seen_hashes.append(h)

        doc["text"] = "\n\n".join(unique)
        doc["duplicates_removed"] = removed
        logger.info(f"[清洗] 去重: 移除 {removed} 个重复段落")
        return doc

    def _simhash(self, text: str) -> int:
        """简化版 SimHash — 64 位"""
        features = re.findall(r'[\w\u4e00-\u9fff]+', text.lower())
        if not features:
            return 0
        v = [0] * 64
        for f in features:
            h = int(hashlib.md5(f.encode()).hexdigest(), 16)
            for i in range(64):
                if h & (1 << i):
                    v[i] += 1
                else:
                    v[i] -= 1
        return sum(1 << i for i in range(64) if v[i] > 0)

    def _hamming_similarity(self, h1: int, h2: int) -> float:
        xor = h1 ^ h2
        diff = bin(xor).count("1")
        return 1 - diff / 64


class FormulaStandardizer:
    """公式标准化 — 将常见公式格式统一为 LaTeX"""

    def clean(self, doc: Dict) -> Dict:
        text = doc["text"]
        # 将 Unicode 数学符号替换为 LaTeX
        replacements = {
            "ℏ": r"\hbar",
            "ψ": r"\psi",
            "∂": r"\partial",
            "∑": r"\sum",
            "∫": r"\int",
            "∞": r"\infty",
            "→": r"\rightarrow",
            "≤": r"\leq",
            "≥": r"\geq",
            "≠": r"\neq",
            "≈": r"\approx",
            "·": r"\cdot",
            "×": r"\times",
            "÷": r"\div",
            "±": r"\pm",
            "√": r"\sqrt",
            "α": r"\alpha",
            "β": r"\beta",
            "γ": r"\gamma",
            "δ": r"\delta",
            "ε": r"\epsilon",
            "θ": r"\theta",
            "λ": r"\lambda",
            "μ": r"\mu",
            "ν": r"\nu",
            "π": r"\pi",
            "ρ": r"\rho",
            "σ": r"\sigma",
            "τ": r"\tau",
            "φ": r"\phi",
            "ω": r"\omega",
            "Δ": r"\Delta",
            "Λ": r"\Lambda",
            "Σ": r"\Sigma",
            "Φ": r"\Phi",
            "Ω": r"\Omega",
            "Θ": r"\Theta",
        }
        for unicode_char, latex in replacements.items():
            text = text.replace(unicode_char, latex)
        doc["text"] = text
        return doc


class BoilerplateCleaner:
    """去除样板文字"""

    BOILERPLATE_PATTERNS = [
        r"第\s*\d+\s*页\s*/\s*共\s*\d+\s*页",
        r"-+\s*\d+\s*-+",
        r"Copyright\s*©.*?\d{4}",
        r"版权所有.*?侵权必究",
        r"\[1\]\s*",  # 参考文献标记
    ]

    def clean(self, doc: Dict) -> Dict:
        text = doc["text"]
        for pattern in self.BOILERPLATE_PATTERNS:
            text = re.sub(pattern, "", text, flags=re.IGNORECASE)
        # 去除多余空行
        text = re.sub(r"\n{3,}", "\n\n", text)
        doc["text"] = text
        return doc


# ============================================================
# AST-aware 分块器
# ============================================================

class ASTAwareChunker:
    """AST 感知分块 — 保持代码块和公式完整"""

    def __init__(self, max_chunk_size: int = 512, overlap: int = 64,
                 preserve_code_blocks: bool = True, preserve_formulas: bool = True):
        self.max_size = max_chunk_size
        self.overlap = overlap
        self.preserve_code = preserve_code_blocks
        self.preserve_formulas = preserve_formulas

    def chunk(self, doc: Dict) -> List[Dict]:
        text = doc["text"]
        paragraphs = text.split("\n\n")

        chunks = []
        current_chunk = ""
        current_section = ""

        for para in paragraphs:
            para = para.strip()
            if not para:
                continue

            # 识别标题
            if para.startswith("#"):
                current_section = para.split("\n")[0][:100]

            # 识别代码块（``` 包裹）
            if para.startswith("```") and self.preserve_code:
                if current_chunk:
                    chunks.append({"text": current_chunk, "section": current_section})
                chunks.append({"text": para, "section": current_section, "type": "code"})
                current_chunk = ""
                continue

            # 识别公式块（$$ 包裹）
            if "$$" in para and self.preserve_formulas:
                if current_chunk:
                    chunks.append({"text": current_chunk, "section": current_section})
                chunks.append({"text": para, "section": current_section, "type": "formula"})
                current_chunk = ""
                continue

            # 普通段落 — 累积
            if len(current_chunk) + len(para) > self.max_size:
                if current_chunk:
                    chunks.append({"text": current_chunk, "section": current_section})
                current_chunk = para
            else:
                current_chunk = current_chunk + "\n\n" + para if current_chunk else para

        if current_chunk:
            chunks.append({"text": current_chunk, "section": current_section})

        # 添加 overlap
        if self.overlap > 0 and len(chunks) > 1:
            chunks = self._add_overlap(chunks)

        logger.info(f"[分块] 共 {len(chunks)} 块, 平均长度 {sum(len(c['text']) for c in chunks) // max(len(chunks), 1)}")
        return chunks

    def _add_overlap(self, chunks: List[Dict]) -> List[Dict]:
        result = []
        for i, chunk in enumerate(chunks):
            if i > 0 and chunk.get("type") not in ("code", "formula"):
                prev_tail = chunks[i - 1]["text"][-self.overlap:]
                chunk["text"] = prev_tail + "\n\n" + chunk["text"]
            result.append(chunk)
        return result


# ============================================================
# Pipeline 主类
# ============================================================

class DocumentIngestionPipeline:
    """文档摄入清洗 pipeline — P1-7 优化"""

    def __init__(self):
        self.cleaners = [
            HeaderFooterCleaner(),
            DuplicateParagraphCleaner(threshold=0.9),
            FormulaStandardizer(),
            BoilerplateCleaner(),
        ]
        self.chunker = ASTAwareChunker(
            max_chunk_size=512,
            overlap=64,
            preserve_code_blocks=True,
            preserve_formulas=True,
        )

    def ingest(self, file_path: str, metadata: Optional[Dict] = None) -> List[Dict]:
        """完整 pipeline

        Args:
            file_path: 文档路径
            metadata: 额外元数据

        Returns:
            [{"text": ..., "metadata": ...}, ...] 分块后的文档列表
        """
        path = Path(file_path)
        if not path.exists():
            raise FileNotFoundError(f"文件不存在: {file_path}")

        logger.info(f"[Pipeline] 开始处理: {path.name}")

        # 1. 格式解析
        ext = path.suffix.lower()
        parser = PARSER_REGISTRY.get(ext)
        if parser is None:
            logger.warning(f"不支持的格式 {ext}，用纯文本解析")
            parser = TextParser()

        doc = parser.parse(str(path))
        original_len = len(doc["text"])
        logger.info(f"[Pipeline] 解析完成: {original_len} 字符")

        # 2. 清洗
        for cleaner in self.cleaners:
            doc = cleaner.clean(doc)

        cleaned_len = len(doc["text"])
        logger.info(f"[Pipeline] 清洗完成: {cleaned_len} 字符 (去除 {original_len - cleaned_len})")

        # 3. 元数据标注
        file_hash = self._file_hash(str(path))
        base_metadata = {
            **(metadata or {}),
            "source_file": path.name,
            "file_path": str(path),
            "ingested_at": datetime.now().isoformat(),
            "file_hash": file_hash,
            "page_count": doc.get("page_count", 1),
        }

        # 4. AST-aware 分块
        chunks = self.chunker.chunk(doc)

        # 5. 每个块加元数据
        enriched = []
        for i, chunk in enumerate(chunks):
            enriched.append({
                "page_content": chunk["text"],
                "metadata": {
                    **base_metadata,
                    "chunk_id": i,
                    "total_chunks": len(chunks),
                    "section": chunk.get("section", ""),
                    "chunk_type": chunk.get("type", "text"),
                },
            })

        logger.info(f"[Pipeline] 完成: {len(enriched)} 块")
        return enriched

    def _file_hash(self, file_path: str) -> str:
        h = hashlib.md5()
        with open(file_path, "rb") as f:
            for chunk in iter(lambda: f.read(4096), b""):
                h.update(chunk)
        return h.hexdigest()

    def ingest_to_pgvector(self, file_path: str, metadata: Optional[Dict] = None) -> int:
        """摄入到 pgvector 向量库

        Returns:
            添加的文档数
        """
        from rag.retriever import get_rag_retriever
        chunks = self.ingest(file_path, metadata)
        retriever = get_rag_retriever()
        retriever.add_documents(chunks)
        return len(chunks)


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(name)s] %(message)s")
    parser = argparse.ArgumentParser()
    parser.add_argument("--ingest", type=str, help="摄入单个文档")
    parser.add_argument("--ingest-dir", type=str, help="摄入目录下所有文档")
    args = parser.parse_args()

    pipeline = DocumentIngestionPipeline()

    if args.ingest:
        count = pipeline.ingest_to_pgvector(args.ingest)
        print(f"已添加 {count} 个文档块到 pgvector")

    if args.ingest_dir:
        d = Path(args.ingest_dir)
        total = 0
        for f in d.iterdir():
            if f.suffix.lower() in PARSER_REGISTRY:
                try:
                    count = pipeline.ingest_to_pgvector(str(f))
                    total += count
                    print(f"  {f.name}: {count} 块")
                except Exception as e:
                    print(f"  {f.name}: 失败 - {e}")
        print(f"总计添加 {total} 个文档块")
