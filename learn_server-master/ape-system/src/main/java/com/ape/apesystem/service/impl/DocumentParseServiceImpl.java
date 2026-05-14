package com.ape.apesystem.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ape.apesystem.domain.PhysicsQuestionPaper;
import com.ape.apesystem.service.DocumentParseService;
import com.ape.apesystem.service.PhysicsQuestionPaperService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 文档解析服务实现
 * @date 2024-01-24 10:00
 */
@Service
public class DocumentParseServiceImpl implements DocumentParseService {

    @Autowired
    private PhysicsQuestionPaperService physicsQuestionPaperService;

    @PostConstruct
    public void init() {
        // 应用启动初始化
        System.out.println("=== 应用启动完成 ===");
    }

    @Override
    public Map<String, Object> parseDocument(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String fileExtension = getFileExtension(file.getOriginalFilename());

            String content;
            if ("pdf".equalsIgnoreCase(fileExtension)) {
                content = parsePdf(file);
            } else if ("doc".equalsIgnoreCase(fileExtension)) {
                content = parseDoc(file);
            } else if ("docx".equalsIgnoreCase(fileExtension)) {
                content = parseDocx(file);
            } else {
                throw new IllegalArgumentException("不支持的文件格式: " + fileExtension);
            }

            // 首先输出原始内容用于调试
            System.out.println("=== 原始文档内容 ===");
            System.out.println(content);
            System.out.println("=== 原始文档内容结束 ===");

            content = normalizeContent(content);

            // 解析题目和答案
            Map<String, Object> parsedData = extractQuestionsAndAnswers(content);
            
            result.put("success", true);
            result.put("message", "文档解析成功");
            result.put("data", parsedData);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文档解析失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public PhysicsQuestionPaper parseAndSave(MultipartFile file, String paperTitle, String subjectBranch) {
        Map<String, Object> parseResult = parseDocument(file);
        
        if (!(Boolean) parseResult.get("success")) {
            throw new RuntimeException((String) parseResult.get("message"));
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) parseResult.get("data");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) data.get("questions");
        String answerContent = (String) data.get("answers");
        Integer totalQuestions = (Integer) data.get("totalQuestions");

        PhysicsQuestionPaper paper = new PhysicsQuestionPaper()
                .setId(String.valueOf(IdWorker.getId()))
                .setPaperTitle(paperTitle)
                .setSubjectBranch(subjectBranch)
                .setDocumentPath(getFilePath(file))
                .setTotalQuestions(totalQuestions)
                .setQuestionContent(JSON.toJSONString(questions))
                .setAnswerContent(answerContent);

        physicsQuestionPaperService.save(paper);
        return paper;
    }

    /**
     * 测试读取物理题库文档
     */
    public void testReadPhysicsDocument() {
        try {
            String filePath = "c:/Users/ACER/Desktop/AI learning buddy/物理题库/运动学/1.高中物理运动学专项训练题.docx";
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.err.println("文件不存在: " + filePath);
                return;
            }
            
            System.out.println("=== 开始读取物理题库文档 ===");
            System.out.println("文件路径: " + filePath);
            System.out.println("文件大小: " + file.length() + " 字节");
            
            FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            List<String> allText = new ArrayList<>();
            
            System.out.println("总段落数: " + paragraphs.size());
            System.out.println();
            
            // 收集所有非空段落
            int validParagraphCount = 0;
            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph para = paragraphs.get(i);
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    validParagraphCount++;
                    allText.add(text);
                    
                    System.out.println("段落 " + validParagraphCount + " (原始位置:" + (i+1) + "): " + text);
                    
                    // 如果段落包含题号，显示详细信息
                    if (text.matches(".*\\d+[\\.．、].*")) {
                        System.out.println("  [检测到题号]");
                        
                        // 检查题型
                        if (text.contains("A.") || text.contains("B.") || text.contains("C.") || text.contains("D.")) {
                            System.out.println("  [题型: 选择题]");
                        } else if (text.contains("____") || text.contains("（ ）") || text.contains("填空")) {
                            System.out.println("  [题型: 填空题]");
                        } else if (text.contains("解：") || text.contains("答：") || text.contains("计算")) {
                            System.out.println("  [题型: 计算题]");
                        }
                        
                        // 显示run信息
                        List<XWPFRun> runs = para.getRuns();
                        if (runs.size() > 1) {
                            System.out.println("  [Run数量: " + runs.size() + "]");
                            for (int j = 0; j < Math.min(3, runs.size()); j++) { // 只显示前3个run
                                XWPFRun run = runs.get(j);
                                String runText = run.getText(0);
                                if (runText != null && !runText.trim().isEmpty()) {
                                    System.out.println("    Run " + j + ": " + runText);
                                }
                            }
                            if (runs.size() > 3) {
                                System.out.println("    ... (还有" + (runs.size() - 3) + "个run)");
                            }
                        }
                    }
                    
                    System.out.println();
                }
            }
            
            document.close();
            fis.close();
            
            System.out.println("=== 文档读取完成 ===");
            System.out.println("有效段落数: " + validParagraphCount);
            
            // 分析文档结构
            analyzeStructure(allText);
            
            // 测试新的解析算法 - 基于段落的智能合并
            System.out.println("\n=== 测试新的解析算法（基于段落合并） ===");
            List<Map<String, Object>> questions = extractQuestionsFromParagraphs(paragraphs);
            
            System.out.println("解析结果:");
            System.out.println("提取到的题目数量: " + questions.size());
            
            // 显示前5道题
            for (int i = 0; i < Math.min(5, questions.size()); i++) {
                Map<String, Object> question = questions.get(i);
                System.out.println("\n题目 " + (i+1) + ":");
                System.out.println("  题号: " + question.get("questionNumber"));
                System.out.println("  类型: " + question.get("type"));
                System.out.println("  题干: " + question.get("title"));
                
                @SuppressWarnings("unchecked")
                Map<String, String> options = (Map<String, String>) question.get("options");
                if (!options.isEmpty()) {
                    System.out.println("  选项:");
                    for (Map.Entry<String, String> entry : options.entrySet()) {
                        System.out.println("    " + entry.getKey() + ". " + entry.getValue());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("读取文档时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void analyzeStructure(List<String> paragraphs) {
        System.out.println("\n=== 文档结构分析 ===");
        
        // 分析题型分布
        List<Integer> choiceQuestions = new ArrayList<>();
        List<Integer> fillInQuestions = new ArrayList<>();
        List<Integer> calculationQuestions = new ArrayList<>();
        List<Integer> unknownQuestions = new ArrayList<>();
        
        int totalQuestions = 0;
        
        for (int i = 0; i < paragraphs.size(); i++) {
            String text = paragraphs.get(i);
            
            // 检查是否包含题号
            if (text.matches(".*\\d+[\\.．、].*")) {
                totalQuestions++;
                
                // 判断题型
                if (text.contains("A.") || text.contains("B.") || text.contains("C.") || text.contains("D.")) {
                    choiceQuestions.add(i + 1);
                } else if (text.contains("____") || text.contains("（ ）") || text.contains("填空")) {
                    fillInQuestions.add(i + 1);
                } else if (text.contains("解：") || text.contains("答：") || text.contains("计算")) {
                    calculationQuestions.add(i + 1);
                } else {
                    unknownQuestions.add(i + 1);
                }
            }
        }
        
        System.out.println("总题数: " + totalQuestions);
        System.out.println("选择题数量: " + choiceQuestions.size() + " (段落号: " + choiceQuestions + ")");
        System.out.println("填空题数量: " + fillInQuestions.size() + " (段落号: " + fillInQuestions + ")");
        System.out.println("计算题数量: " + calculationQuestions.size() + " (段落号: " + calculationQuestions + ")");
        System.out.println("未分类题数量: " + unknownQuestions.size() + " (段落号: " + unknownQuestions + ")");
        
        // 显示前5个段落用于分析
        System.out.println("\n前5个段落内容:");
        for (int i = 0; i < Math.min(5, paragraphs.size()); i++) {
            System.out.println((i+1) + ". " + paragraphs.get(i));
        }
        
        if (paragraphs.size() > 5) {
            System.out.println("... (还有" + (paragraphs.size() - 5) + "个段落)");
        }
    }

    /**
     * 解析PDF文件 - 修复：确保提取所有页面内容
     */
    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            if (document.isEncrypted()) {
                document.setAllSecurityToBeRemoved(true);
            }
            
            int totalPages = document.getNumberOfPages();
            System.out.println("=== PDF解析开始，总页数: " + totalPages + " ===");
            
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setLineSeparator("\n");
            stripper.setParagraphStart("\n");
            stripper.setParagraphEnd("\n");
            stripper.setWordSeparator(" ");
            stripper.setSuppressDuplicateOverlappingText(true);
            
            stripper.setStartPage(1);
            stripper.setEndPage(totalPages);
            
            String text = stripper.getText(document);
            
            System.out.println("=== PDF解析完成，提取字符数: " + (text != null ? text.length() : 0) + " ===");
            
            if (text == null || text.trim().isEmpty()) {
                System.out.println("PDF文本为空，尝试逐页提取...");
                text = extractPdfPageByPage(document);
            }
            
            return text != null ? text : "";
        } catch (Exception e) {
            System.err.println("PDF解析失败: " + e.getMessage());
            e.printStackTrace();
            return parsePdfFallback(file);
        }
    }
    
    /**
     * 逐页提取PDF内容（备用方案）
     */
    private String extractPdfPageByPage(PDDocument document) throws IOException {
        StringBuilder allText = new StringBuilder();
        int totalPages = document.getNumberOfPages();
        
        for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
            try {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                stripper.setSortByPosition(true);
                stripper.setLineSeparator("\n");
                
                String pageText = stripper.getText(document);
                if (pageText != null && !pageText.trim().isEmpty()) {
                    allText.append(pageText).append("\n");
                    System.out.println("第 " + pageNum + " 页提取字符数: " + pageText.length());
                }
            } catch (Exception e) {
                System.err.println("提取第 " + pageNum + " 页失败: " + e.getMessage());
            }
        }
        
        return allText.toString();
    }
    
    /**
     * PDF解析备用方法
     */
    private String parsePdfFallback(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            if (document.isEncrypted()) {
                document.setAllSecurityToBeRemoved(true);
            }
            
            int totalPages = document.getNumberOfPages();
            StringBuilder allText = new StringBuilder();
            
            for (int i = 1; i <= totalPages; i++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                stripper.setSortByPosition(true);
                String pageText = stripper.getText(document);
                if (pageText != null) {
                    allText.append(pageText).append("\n");
                }
            }
            
            return allText.toString();
        }
    }

    /**
     * 解析DOC文件
     */
    private String parseDoc(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             WordExtractor extractor = new WordExtractor(inputStream)) {
            return extractor.getText();
        }
    }

    /**
     * 解析DOCX文件，保留更多格式信息
     */
    private String parseDocx(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {
            
            StringBuilder sb = new StringBuilder();
            
            // 获取文档中的所有元素（段落和表格）
            List<IBodyElement> elements = document.getBodyElements();
            
            for (IBodyElement element : elements) {
                if (element.getElementType() == BodyElementType.PARAGRAPH) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    String paragraphText = extractParagraphWithFormat(paragraph);
                    if (paragraphText != null && !paragraphText.trim().isEmpty()) {
                        sb.append(paragraphText).append("\n");
                    }
                } else if (element.getElementType() == BodyElementType.TABLE) {
                    XWPFTable table = (XWPFTable) element;
                    // 提取表格内容
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                                String paragraphText = extractParagraphWithFormat(paragraph);
                                if (paragraphText != null && !paragraphText.trim().isEmpty()) {
                                    sb.append(paragraphText).append(" ");
                                }
                            }
                            sb.append("   "); // 单元格之间加空格
                        }
                        sb.append("\n"); // 行之间换行
                    }
                }
            }
            
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("解析DOCX文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取段落内容并保留格式信息
     */
    private String extractParagraphWithFormat(XWPFParagraph para) {
        List<XWPFRun> runs = para.getRuns();
        StringBuilder content = new StringBuilder();

        for (XWPFRun run : runs) {
            String runText = run.getText(0);
            if (runText == null) continue;

            // 检查是否有特殊格式（如数学公式）
            if (hasMathFormat(run)) {
                content.append(runText);
            } else {
                content.append(runText);
            }
        }

        return content.toString();
    }

    /**
     * 检查运行是否包含数学格式
     */
    private boolean hasMathFormat(XWPFRun run) {
        // 简单的启发式检测：检查是否包含常见的数学符号
        String text = run.getText(0);
        if (text == null) return false;

        // 检查是否包含数学符号或公式相关字符
        return text.matches(".*[₀₁₂₃₄₅₆₇₈₉⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾αβγδεθλμπρσφχψω∑∫∂√∞∝≠≤≥±×÷∈∋∝].*") ||
               text.contains("²") || text.contains("³") || 
               text.contains("½") || text.contains("¼") || text.contains("¾") ||
               text.contains("√") || text.contains("∫") || text.contains("∑");
    }

    /**
     * 尝试提取数学公式为LaTeX格式
     */
    private String extractMathAsLatex(XWPFRun run) {
        String text = run.getText(0);
        if (text == null) return null;

        // 简单的数学符号替换为LaTeX格式
        text = text.replaceAll("₀", "_0").replaceAll("₁", "_1").replaceAll("₂", "_2")
                   .replaceAll("₃", "_3").replaceAll("₄", "_4").replaceAll("₅", "_5")
                   .replaceAll("₆", "_6").replaceAll("₇", "_7").replaceAll("₈", "_8")
                   .replaceAll("₉", "_9")
                   .replaceAll("⁰", "^0").replaceAll("¹", "^1").replaceAll("²", "^2")
                   .replaceAll("³", "^3").replaceAll("⁴", "^4").replaceAll("⁵", "^5")
                   .replaceAll("⁶", "^6").replaceAll("⁷", "^7").replaceAll("⁸", "^8")
                   .replaceAll("⁹", "^9");

        // 替换一些常见的数学符号
        text = text.replace("α", "\\alpha").replace("β", "\\beta").replace("γ", "\\gamma")
                   .replace("δ", "\\delta").replace("ε", "\\epsilon").replace("θ", "\\theta")
                   .replace("λ", "\\lambda").replace("μ", "\\mu").replace("π", "\\pi")
                   .replace("ρ", "\\rho").replace("σ", "\\sigma").replace("φ", "\\phi")
                   .replace("χ", "\\chi").replace("ψ", "\\psi").replace("ω", "\\omega")
                   .replace("∑", "\\sum").replace("∫", "\\int").replace("∂", "\\partial")
                   .replace("√", "\\sqrt{}").replace("∞", "\\infty").replace("∝", "\\propto")
                   .replace("≠", "\\neq").replace("≤", "\\leq").replace("≥", "\\geq")
                   .replace("±", "\\pm").replace("×", "\\times").replace("÷", "\\div")
                   .replace("∈", "\\in").replace("∋", "\\ni");

        return text;
    }

    /**
     * 从文档内容中提取题目和答案
     */
    private Map<String, Object> extractQuestionsAndAnswers(String content) {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> questions = new ArrayList<>();
        
        // 使用改进的解析算法
        questions.addAll(extractQuestionsWithSmartParsing(content));

        result.put("questions", questions);
        result.put("answers", extractAnswers(content));
        result.put("totalQuestions", questions.size());
        
        return result;
    }
    
    /**
     * 智能题目提取方法：改进的段落合并和题目识别逻辑
     */
    private List<Map<String, Object>> extractQuestionsWithSmartParsing(String content) {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        content = normalizeContent(content);
        
        String[] lines = content.split("\n");
        
        StringBuilder currentQuestion = new StringBuilder();
        String currentQuestionNumber = null;
        boolean inQuestion = false;
        int questionCount = 0;
        String currentSectionType = null; // 当前章节类型提示
        
        // 更精确地寻找答案部分的起始位置
        int answerStartIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("参考答案") || line.contains("答案解析") || 
                line.contains("答案与解析") || line.contains("参考答案及解析") ||
                line.matches(".*[一二三四五六七八九十][、\\.]\\s*答案.*")) {
                answerStartIndex = i;
                break;
            }
        }
        
        int maxLineIndex = answerStartIndex >= 0 ? answerStartIndex : lines.length;
        
        for (int i = 0; i < maxLineIndex; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // 识别章节标题并设置当前题型提示
            if (line.matches("^[一二三四五六七八九十]+、.*")) {
                // 在切换章节前，先保存当前正在构建的题目
                if (inQuestion && currentQuestionNumber != null && currentQuestion.length() > 0) {
                    String questionBody = currentQuestion.toString().trim();
                    if (isValidQuestionBody(questionBody)) {
                        addQuestionToList(questions, currentQuestionNumber, questionBody, currentSectionType);
                        questionCount++;
                    }
                }
                // 重置当前题目状态
                currentQuestion = new StringBuilder();
                currentQuestionNumber = null;
                inQuestion = false;
                
                // 设置新的章节类型
                if (line.contains("选择") || line.contains("单选") || line.contains("多选")) {
                    currentSectionType = "choice";
                } else if (line.contains("填空")) {
                    currentSectionType = "fillIn";
                } else if (line.contains("判断")) {
                    currentSectionType = "trueFalse";
                } else if (line.contains("计算") || line.contains("解答") || line.contains("问答")) {
                    currentSectionType = "calculation";
                }
                continue; // 跳过章节标题
            }
            
            // 匹配题号（更灵活的正则表达式，支持空格和更多符号）
            Pattern questionStartPattern = Pattern.compile("^(\\d+|[①②③④⑤⑥⑦⑧⑨⑩])[\\.．、\\)\\s]\\s*(.*)");
            Matcher matcher = questionStartPattern.matcher(line);
            
            boolean isOptionLine = line.matches("^[A-D][\\.．、)]\\s*.*");
            
            if (matcher.find()) {
                if (inQuestion && currentQuestionNumber != null && currentQuestion.length() > 0) {
                    String questionBody = currentQuestion.toString().trim();
                    if (isValidQuestionBody(questionBody)) {
                        addQuestionToList(questions, currentQuestionNumber, questionBody, currentSectionType);
                        questionCount++;
                    }
                }
                
                currentQuestionNumber = matcher.group(1);
                currentQuestion = new StringBuilder(matcher.group(2));
                inQuestion = true;
            } else if (inQuestion) {
                // 检查是否是选项行
                if (isOptionLine || line.matches("^\\s*[A-D][\\.．、)]\\s*.*")) {
                    if (currentQuestion.length() > 0) {
                        currentQuestion.append("\n");
                    }
                    currentQuestion.append(line);
                } else if (!line.contains("答案") && !line.contains("解析") && 
                         !line.contains("参考答案") && 
                         !line.matches("^[一二三四五六七八九十]+、.*")) {
                    // 如果当前行看起来像是新题目的开始，结束当前题目
                    if (line.matches("^\\d+[\\.．、\\)\\s]\\s*.*") || 
                        line.matches("^[①②③④⑤⑥⑦⑧⑨⑩][\\.．、\\)\\s]\\s*.*")) {
                        if (currentQuestion.length() > 0) {
                            String questionBody = currentQuestion.toString().trim();
                            if (isValidQuestionBody(questionBody)) {
                                addQuestionToList(questions, currentQuestionNumber, questionBody, currentSectionType);
                                questionCount++;
                            }
                        }
                        // 开始新题目
                        Matcher newMatcher = Pattern.compile("^(\\d+|[①②③④⑤⑥⑦⑧⑨⑩])[\\.．、\\)\\s]\\s*(.*)").matcher(line);
                        if (newMatcher.find()) {
                            currentQuestionNumber = newMatcher.group(1);
                            currentQuestion = new StringBuilder(newMatcher.group(2));
                        }
                    } else {
                        // 添加到当前题目
                        if (currentQuestion.length() > 0) {
                            currentQuestion.append(" ");
                        }
                        currentQuestion.append(line);
                    }
                }
            }
        }
        
        if (inQuestion && currentQuestionNumber != null && currentQuestion.length() > 0) {
            String questionBody = currentQuestion.toString().trim();
            if (isValidQuestionBody(questionBody)) {
                addQuestionToList(questions, currentQuestionNumber, questionBody, currentSectionType);
            }
        }
        
        return questions;
    }
    
    /**
     * 检查是否是有效的题目内容
     */
    private boolean isValidQuestionBody(String questionBody) {
        if (questionBody.length() < 10) return false; // 太短
        if (questionBody.contains("答案") || questionBody.contains("解析") || questionBody.contains("参考")) return false; // 答案部分
        if (questionBody.matches("^\\s*[A-D]\\s*[\\.．、]\\s*.*")) return false; // 只是选项
        if (questionBody.matches("^[一二三四五六七八九十]+、.*")) return false; // 题型分类标题
        return true;
    }
    
    /**
     * 提取答案内容
     */
    private String extractAnswers(String content) {
        StringBuilder answers = new StringBuilder();
        String[] lines = content.split("\n");
        
        boolean inAnswerSection = false;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.contains("参考答案") || line.contains("答案")) {
                inAnswerSection = true;
                answers.append(line).append("\n");
            } else if (inAnswerSection) {
                // 检查是否进入下一部分（如题目解析）
                if (line.contains("解析") || line.contains("说明") || line.matches("^\\d+[\\.．、].*")) {
                    break;
                }
                answers.append(line).append("\n");
            }
        }
        
        return answers.toString().trim();
    }
    
    /**
     * 将题目添加到列表中 - 修复：正确分离题干和选项
     */
    private void addQuestionToList(List<Map<String, Object>> questions, String questionNumber, String questionBody, String sectionTypeHint) {
        Map<String, String> options = extractOptionsFromText(questionBody);
        
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", questions.size() + 1);
        item.put("questionNumber", questionNumber);
        
        int questionType = determineQuestionType(questionBody, options, sectionTypeHint);
        
        item.put("type", questionType);
        
        if (questionType == 0 && !options.isEmpty()) {
            String stem = extractQuestionStem(questionBody, options);
            item.put("title", stem);
            item.put("options", options);
        } else if (questionType == 0 && options.isEmpty()) {
            String stem = questionBody.replaceAll("(?m)^\\s*[A-D][\\.．、\\)]\\s*.*$", "");
            stem = stem.replaceAll("\n{2,}", "\n").trim();
            item.put("title", stem);
            Map<String, String> extractedOptions = new LinkedHashMap<>();
            Pattern optionPattern = Pattern.compile("[A-D][\\.．、\\)]\\s*([^A-D\\.．、\\)]*?)(?=\\s+[A-D][\\.．、\\)]|$)");
            Matcher matcher = optionPattern.matcher(questionBody);
            while (matcher.find()) {
                String key = matcher.group(0).substring(0, 1);
                String value = matcher.group(1).trim();
                if (!value.isEmpty()) {
                    extractedOptions.put(key, value);
                }
            }
            item.put("options", extractedOptions);
        } else if (questionType == 1) {
            item.put("title", formatFillInQuestionContent(questionNumber, questionBody));
        } else if (questionType == 2) {
            item.put("title", formatCalculationQuestionContent(questionNumber, questionBody));
        } else if (questionType == 3) {
            item.put("title", formatTrueFalseQuestionContent(questionNumber, questionBody));
        } else {
            item.put("title", questionNumber + ". " + questionBody);
        }
        
        questions.add(item);
    }
    
    private int determineQuestionType(String questionBody, Map<String, String> options, String sectionTypeHint) {
        if (sectionTypeHint != null) {
            switch (sectionTypeHint) {
                case "choice": return 0;
                case "fillIn": return 1;
                case "calculation": return 2;
                case "trueFalse": return 3;
            }
        }
        
        if (options != null && options.size() >= 2) {
            return 0;
        }
        
        if (questionBody == null || questionBody.isEmpty()) {
            return 1;
        }
        
        boolean hasChoicePattern = questionBody.contains("A.") || questionBody.contains("A．") || 
                                  questionBody.contains("A、") || questionBody.contains("A)");
        boolean hasB = questionBody.contains("B.") || questionBody.contains("B．") || 
                      questionBody.contains("B、") || questionBody.contains("B)");
        boolean hasC = questionBody.contains("C.") || questionBody.contains("C．") || 
                      questionBody.contains("C、") || questionBody.contains("C)");
        boolean hasD = questionBody.contains("D.") || questionBody.contains("D．") || 
                      questionBody.contains("D、") || questionBody.contains("D)");
        
        if (hasChoicePattern && (hasB || hasC || hasD)) {
            return 0;
        }
        
        boolean hasTrueFalsePattern = questionBody.matches(".*[。！？]\\s*（\\s*[√×]\\s*）\\s*$") ||
                                      questionBody.matches(".*[。！？]\\s*（\\s*[正确错误]\\s*）\\s*$") ||
                                      questionBody.matches(".*[。！？]\\s*（\\s*[对错]\\s*）\\s*$") ||
                                      questionBody.contains("（ √ ）") || questionBody.contains("（ × ）");
        if (hasTrueFalsePattern) {
            return 3;
        }
        
        boolean hasBlank = questionBody.contains("____") || questionBody.contains("______") ||
                          questionBody.matches(".*_{2,}.*") || questionBody.contains("（  ）") ||
                          questionBody.contains("（　）") || questionBody.contains("（  )");
        boolean hasParenthesesBlank = questionBody.matches(".*（\\s*）.*") || 
                                     questionBody.matches(".*\\(\\s*\\).*");
        boolean hasFillInKeywords = questionBody.contains("填空") || questionBody.contains("空格");
        
        if (hasBlank || hasParenthesesBlank || hasFillInKeywords) {
            return 1;
        }
        
        boolean hasCalculationKeywords = questionBody.contains("求：") || questionBody.contains("求 ") || 
                                       questionBody.contains("计算") || questionBody.contains("证明") ||
                                       questionBody.contains("推导") || questionBody.contains("解答") ||
                                       questionBody.contains("解：") || questionBody.contains("答：") ||
                                       questionBody.matches(".*（\\d+分\\）.*") ||
                                       questionBody.matches(".*[，。]已知.*求.*");
        boolean hasMultiSentence = questionBody.split("[。！？]").length > 2;
        
        if (hasCalculationKeywords || (hasMultiSentence && !hasBlank)) {
            return 2;
        }
        
        boolean hasTrueFalseKeywords = questionBody.contains("判断") || questionBody.contains("是否") ||
                                     questionBody.contains("正确") || questionBody.contains("错误") ||
                                     questionBody.contains("对错");
        if (hasTrueFalseKeywords) {
            return 3;
        }
        
        if (questionBody.length() < 100) {
            return 1;
        }
        
        return 2;
    }
    
    /**
     * 从段落中提取题目（基于段落合并的智能算法）
     */
    private List<Map<String, Object>> extractQuestionsFromParagraphs(List<XWPFParagraph> paragraphs) {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        // 首先收集所有非空段落文本
        List<String> validTexts = new ArrayList<>();
        for (XWPFParagraph para : paragraphs) {
            String text = para.getText();
            if (text != null && !text.trim().isEmpty()) {
                validTexts.add(text.trim());
            }
        }
        
        // 找到答案部分的开始位置
        int answerStartIndex = -1;
        for (int i = 0; i < validTexts.size(); i++) {
            String text = validTexts.get(i);
            if (text.contains("参考答案") || text.contains("答案")) {
                answerStartIndex = i;
                break;
            }
        }
        
        // 只处理答案之前的部分（真正的题目）
        List<String> questionTexts = answerStartIndex >= 0 ? 
            validTexts.subList(0, answerStartIndex) : validTexts;
        
        // 跳过标题和说明部分，找到真正的题目开始位置
        int questionStartIndex = 0;
        for (int i = 0; i < questionTexts.size(); i++) {
            String text = questionTexts.get(i);
            if (text.contains("一、选择题") || text.contains("二、填空题") || text.contains("三、计算题")) {
                questionStartIndex = i + 1;
                break;
            }
        }
        
        // 使用专门的物理文档分析器
        try {
            PhysicsDocumentAnalyzer analyzer = new PhysicsDocumentAnalyzer();
            PhysicsDocumentAnalyzer.DocumentAnalysis analysis = analyzer.analyzeDocument("c:/Users/ACER/Desktop/AI learning buddy/物理题库/运动学/1.高中物理运动学专项训练题.docx");
            
            // 将分析结果转换为Map格式
            int questionNumber = 1;
            
            for (String questionContent : analysis.questionsByType.get("choice")) {
                addQuestionToList(questions, String.valueOf(questionNumber), questionContent, "choice");
                questionNumber++;
            }
            
            for (String questionContent : analysis.questionsByType.get("trueFalse")) {
                addQuestionToList(questions, String.valueOf(questionNumber), questionContent, "trueFalse");
                questionNumber++;
            }
            
            for (String questionContent : analysis.questionsByType.get("fillIn")) {
                addQuestionToList(questions, String.valueOf(questionNumber), questionContent, "fillIn");
                questionNumber++;
            }
            
            for (String questionContent : analysis.questionsByType.get("calculation")) {
                addQuestionToList(questions, String.valueOf(questionNumber), questionContent, "calculation");
                questionNumber++;
            }
            
        } catch (Exception e) {
            System.err.println("使用物理文档分析器失败，回退到基础解析: " + e.getMessage());
            // 回退到基础解析逻辑
            return extractQuestionsBasic(questionTexts, questionStartIndex);
        }
        
        return questions;
    }
    
    /**
     * 基础题目提取逻辑（回退方案）
     */
    private List<Map<String, Object>> extractQuestionsBasic(List<String> questionTexts, int questionStartIndex) {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        if (questionStartIndex >= questionTexts.size()) {
            return questions;
        }
        
        List<String> actualQuestions = questionTexts.subList(questionStartIndex, questionTexts.size());
        
        int questionNumber = 1;
        StringBuilder currentQuestion = new StringBuilder();
        boolean inQuestion = false;
        
        for (String text : actualQuestions) {
            // 检查是否是选择题（包含选项A.B.C.D.）
            boolean isChoiceQuestion = text.contains("A.") && text.contains("B.") && 
                                     (text.contains("C.") || text.contains("D."));
            
            // 检查是否是填空题（包含填空标记）
            boolean isFillInQuestion = text.contains("____") || text.contains("（ ）");
            
            // 检查是否是计算题（包含求解要求）
            boolean isCalculationQuestion = text.contains("求：") || text.contains("求 ") || 
                                            text.contains("计算") || text.matches(".*\\（\\d+分\\）.*");
            
            // 如果当前有题目在构建中，且新段落看起来像是新题目的开始
            if (inQuestion && (isChoiceQuestion || isFillInQuestion || isCalculationQuestion || 
                              text.matches("^\\d+[\\.．、].*"))) {
                // 保存当前题目
                String questionBody = currentQuestion.toString().trim();
                if (questionBody.length() > 10) {
                    addQuestionToList(questions, String.valueOf(questionNumber), questionBody, null);
                    questionNumber++;
                }
                currentQuestion = new StringBuilder();
            }
            
            // 开始新题目或继续当前题目
            if (isChoiceQuestion || isFillInQuestion || isCalculationQuestion || 
                text.matches("^\\d+[\\.．、].*") || inQuestion) {
                if (currentQuestion.length() > 0) {
                    currentQuestion.append("\n");
                }
                currentQuestion.append(text);
                inQuestion = true;
            }
        }
        
        // 处理最后一道题
        if (inQuestion && currentQuestion.length() > 10) {
            addQuestionToList(questions, String.valueOf(questionNumber), currentQuestion.toString().trim(), null);
        }
        
        return questions;
    }
    
    /**
     * 物理题库文档分析器
     * 专门分析物理题库Word文档的结构
     */
    private static class PhysicsDocumentAnalyzer {
        
        public static class DocumentAnalysis {
            public List<String> allParagraphs = new ArrayList<>();
            public List<String> questionParagraphs = new ArrayList<>();
            public List<String> answerParagraphs = new ArrayList<>();
            public int answerSectionStart = -1;
            public Map<String, List<String>> questionsByType = new HashMap<>();
            
            public DocumentAnalysis() {
            questionsByType.put("choice", new ArrayList<>());
            questionsByType.put("fillIn", new ArrayList<>());
            questionsByType.put("calculation", new ArrayList<>());
            questionsByType.put("trueFalse", new ArrayList<>());
        }
        }
        
        public DocumentAnalysis analyzeDocument(String filePath) throws Exception {
            DocumentAnalysis analysis = new DocumentAnalysis();
            
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                throw new java.io.FileNotFoundException("文件不存在: " + filePath);
            }
            
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            // 收集所有段落
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    analysis.allParagraphs.add(text.trim());
                }
            }
            
            // 找到答案部分的开始位置
            for (int i = 0; i < analysis.allParagraphs.size(); i++) {
                String text = analysis.allParagraphs.get(i);
                if (text.contains("参考答案") || text.contains("答案")) {
                    analysis.answerSectionStart = i;
                    break;
                }
            }
            
            // 分离题目和答案部分
            if (analysis.answerSectionStart >= 0) {
                analysis.questionParagraphs = analysis.allParagraphs.subList(0, analysis.answerSectionStart);
                analysis.answerParagraphs = analysis.allParagraphs.subList(analysis.answerSectionStart, analysis.allParagraphs.size());
            } else {
                analysis.questionParagraphs = analysis.allParagraphs;
            }
            
            // 分类题目
            classifyQuestions(analysis);
            
            document.close();
            fis.close();
            
            return analysis;
        }
        
        private void classifyQuestions(DocumentAnalysis analysis) {
            List<String> questions = analysis.questionParagraphs;
            
            int startIndex = 0;
            for (int i = 0; i < questions.size(); i++) {
                String text = questions.get(i);
                if (text.contains("一、选择题") || text.contains("二、") || text.contains("三、") || text.contains("四、")) {
                    startIndex = i + 1;
                    break;
                }
            }
            
            if (startIndex >= questions.size()) {
                return;
            }
            
            int choiceEnd = findSectionEnd(questions, startIndex, "二、");
            if (choiceEnd < 0) choiceEnd = findSectionEnd(questions, startIndex, "三、");
            if (choiceEnd < 0) choiceEnd = findSectionEnd(questions, startIndex, "四、");
            if (choiceEnd < 0) choiceEnd = questions.size();
            
            List<String> choiceQuestions = extractQuestionsFromParagraphs(questions.subList(startIndex, choiceEnd), "choice");
            analysis.questionsByType.get("choice").addAll(choiceQuestions);
            
            int section2Start = findSectionStart(questions, "二、");
            if (section2Start >= 0) {
                int section2End = findSectionEnd(questions, section2Start, "三、");
                if (section2End < 0) section2End = findSectionEnd(questions, section2Start, "四、");
                if (section2End < 0) section2End = questions.size();
                
                String section2Title = questions.get(section2Start - 1);
                String section2Type = "fillIn";
                if (section2Title.contains("判断")) {
                    section2Type = "trueFalse";
                } else if (section2Title.contains("填空")) {
                    section2Type = "fillIn";
                }
                
                List<String> section2Questions = extractQuestionsFromParagraphs(questions.subList(section2Start, section2End), section2Type);
                analysis.questionsByType.get(section2Type).addAll(section2Questions);
            }
            
            int section3Start = findSectionStart(questions, "三、");
            if (section3Start >= 0) {
                int section3End = findSectionEnd(questions, section3Start, "四、");
                if (section3End < 0) section3End = questions.size();
                
                String section3Title = questions.get(section3Start - 1);
                String section3Type = "calculation";
                if (section3Title.contains("判断")) {
                    section3Type = "trueFalse";
                } else if (section3Title.contains("填空")) {
                    section3Type = "fillIn";
                } else if (section3Title.contains("计算")) {
                    section3Type = "calculation";
                }
                
                List<String> section3Questions = extractQuestionsFromParagraphs(questions.subList(section3Start, section3End), section3Type);
                analysis.questionsByType.get(section3Type).addAll(section3Questions);
            }
            
            int section4Start = findSectionStart(questions, "四、");
            if (section4Start >= 0) {
                String section4Title = questions.get(section4Start - 1);
                String section4Type = "calculation";
                if (section4Title.contains("判断")) {
                    section4Type = "trueFalse";
                } else if (section4Title.contains("计算")) {
                    section4Type = "calculation";
                }
                
                List<String> section4Questions = extractQuestionsFromParagraphs(questions.subList(section4Start, questions.size()), section4Type);
                analysis.questionsByType.get(section4Type).addAll(section4Questions);
            }
        }
        
        private int findSectionStart(List<String> paragraphs, String sectionTitle) {
            for (int i = 0; i < paragraphs.size(); i++) {
                if (paragraphs.get(i).contains(sectionTitle)) {
                    return i + 1;
                }
            }
            return -1;
        }
        
        private int findSectionEnd(List<String> paragraphs, int startIndex, String nextSectionTitle) {
            for (int i = startIndex; i < paragraphs.size(); i++) {
                if (paragraphs.get(i).contains(nextSectionTitle)) {
                    return i;
                }
            }
            return -1;
        }
        
        private List<String> extractQuestionsFromParagraphs(List<String> paragraphs, String questionType) {
            List<String> questions = new ArrayList<>();
            StringBuilder currentQuestion = new StringBuilder();
            
            for (String text : paragraphs) {
                if (text.contains("二、") || text.contains("三、") || text.contains("四、")) {
                    continue;
                }
                
                boolean isNewQuestion = false;
                
                switch (questionType) {
                    case "choice":
                        isNewQuestion = text.contains("A.") && text.contains("B.") && 
                                       (text.contains("C.") || text.contains("D."));
                        break;
                    case "fillIn":
                        isNewQuestion = text.contains("____") || text.contains("（ ）") || 
                                       text.matches("^\\d+[\\.．、].*");
                        break;
                    case "trueFalse":
                        isNewQuestion = text.matches("^\\d+[\\.．、].*") || 
                                       text.matches(".*[。！？]\\s*（\\s*[√×]\\s*）\\s*$");
                        break;
                    case "calculation":
                        isNewQuestion = text.matches(".*（\\d+分）.*") || 
                                       text.contains("求：") || text.contains("求 ") ||
                                       text.matches("^\\d+[\\.．、].*");
                        break;
                }
                
                if (isNewQuestion && currentQuestion.length() > 0) {
                    questions.add(currentQuestion.toString().trim());
                    currentQuestion = new StringBuilder();
                }
                
                if (currentQuestion.length() > 0) {
                    currentQuestion.append("\n");
                }
                currentQuestion.append(text);
            }
            
            if (currentQuestion.length() > 0) {
                questions.add(currentQuestion.toString().trim());
            }
            
            return questions;
        }
    }
    
    /**
     * 检查是否是有效的题目
     */
    private boolean isValidQuestion(String questionBody) {
        if (questionBody.length() < 10) return false; // 太短
        if (questionBody.contains("答案") || questionBody.contains("解析") || questionBody.contains("参考")) return false; // 答案部分
        if (questionBody.matches("^\\s*[A-D]\\s*[\\.．、]\\s*.*")) return false; // 只是选项
        return true;
    }

    /**
     * 从文本中提取选项 - 修复：改进选项提取逻辑，支持更多格式
     */
    private Map<String, String> extractOptionsFromText(String text) {
        Map<String, String> options = new LinkedHashMap<>();
        
        if (text == null || text.isEmpty()) {
            return options;
        }
        
        String[] lines = text.split("\n|\r\n");
        StringBuilder currentOptionText = new StringBuilder();
        String currentOptionKey = null;
        
        Pattern optionStartPattern = Pattern.compile("^\\s*([A-D])[\\.．、\\)]\\s*(.*)$", Pattern.CASE_INSENSITIVE);
        Pattern inlineOptionPattern = Pattern.compile("\\s*([A-D])[\\.．、\\)]\\s*([^A-D\\.．、\\)]*?)(?=\\s+[A-D][\\.．、\\)]|$)", Pattern.CASE_INSENSITIVE);
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            Matcher matcher = optionStartPattern.matcher(line);
            if (matcher.find()) {
                if (currentOptionKey != null && currentOptionText.length() > 0) {
                    options.put(currentOptionKey, currentOptionText.toString().trim());
                }
                currentOptionKey = matcher.group(1).toUpperCase();
                currentOptionText = new StringBuilder(matcher.group(2));
            } else if (currentOptionKey != null) {
                Matcher newMatcher = optionStartPattern.matcher(line);
                if (newMatcher.find()) {
                    if (currentOptionText.length() > 0) {
                        options.put(currentOptionKey, currentOptionText.toString().trim());
                    }
                    currentOptionKey = newMatcher.group(1).toUpperCase();
                    currentOptionText = new StringBuilder(newMatcher.group(2));
                } else {
                    if (currentOptionText.length() > 0) {
                        currentOptionText.append(" ");
                    }
                    currentOptionText.append(line);
                }
            } else {
                Matcher inlineMatcher = inlineOptionPattern.matcher(line);
                while (inlineMatcher.find()) {
                    String key = inlineMatcher.group(1).toUpperCase();
                    String value = inlineMatcher.group(2).trim();
                    if (!value.isEmpty()) {
                        options.put(key, value);
                    }
                }
            }
        }
        
        if (currentOptionKey != null && currentOptionText.length() > 0) {
            options.put(currentOptionKey, currentOptionText.toString().trim());
        }
        
        return options;
    }
    
    /**
     * 从题目正文中提取题干（移除选项部分）
     */
    private String extractQuestionStem(String questionBody, Map<String, String> options) {
        if (questionBody == null || questionBody.isEmpty()) {
            return "";
        }
        
        String stem = questionBody;
        
        if (!options.isEmpty()) {
            stem = stem.replaceAll("(?m)^\\s*[A-D][\\.．、\\)]\\s*.*$", "");
            stem = stem.replaceAll("(?m)\\s+[A-D][\\.．、\\)]\\s*[^\n]*$", "");
            stem = stem.replaceAll("\\s*[A-D][\\.．、\\)]\\s*[^A-D]*?(?=\\s+[A-D][\\.．、\\)]|$)", "");
            stem = stem.replaceAll("\n{2,}", "\n");
            stem = stem.trim();
        }
        
        return stem;
    }

    private String formatQuestionContent(String questionNumber, String questionBody, Map<String, String> options) {
        String stem = questionBody;
        
        if (!options.isEmpty()) {
            stem = extractQuestionStem(questionBody, options);
        } else {
            stem = stem.replaceAll("(?m)^\\s*[A-D][\\.．、\\)]\\s*.*$", "");
            stem = stem.replaceAll("(?m)\\s+[A-D][\\.．、\\)]\\s*[^\n]*$", "");
            stem = stem.replaceAll("\n{2,}", "\n").trim();
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append(questionNumber).append(". ").append(stem);
        return formatted.toString();
    }

    /**
     * 格式化填空题内容
     */
    private String formatFillInQuestionContent(String questionNumber, String questionBody) {
        return questionNumber + ". " + questionBody;
    }
    
    /**
     * 格式化判断题内容
     */
    private String formatTrueFalseQuestionContent(String questionNumber, String questionBody) {
        String formatted = questionBody.replaceAll("\\s*（\\s*[√×]\\s*）\\s*$", "");
        formatted = formatted.replaceAll("\\s*（\\s*[正确错误]\\s*）\\s*$", "");
        formatted = formatted.replaceAll("\\s*\\(\\s*[√×]\\s*\\)\\s*$", "");
        formatted = formatted.replaceAll("\\s*\\(\\s*[正确错误]\\s*\\)\\s*$", "");
        return questionNumber + ". " + formatted.trim();
    }
    
    /**
     * 格式化计算题内容
     */
    private String formatCalculationQuestionContent(String questionNumber, String questionBody) {
        return questionNumber + ". " + questionBody;
    }

    /**
     * 处理数学符号，将其转换为适当的格式
     */
    private String processMathSymbols(String input) {
        if (input == null) {
            return "";
        }
        
        // 处理常见的数学公式格式
        input = input.replaceAll("v₀", "<sub>0</sub>")
                     .replaceAll("v₁", "<sub>1</sub>")
                     .replaceAll("v₂", "<sub>2</sub>")
                     .replaceAll("t₀", "<sub>0</sub>")
                     .replaceAll("t₁", "<sub>1</sub>")
                     .replaceAll("t₂", "<sub>2</sub>")
                     .replaceAll("x₀", "<sub>0</sub>")
                     .replaceAll("x₁", "<sub>1</sub>")
                     .replaceAll("x₂", "<sub>2</sub>")
                     .replaceAll("a₀", "<sub>0</sub>")
                     .replaceAll("a₁", "<sub>1</sub>")
                     .replaceAll("a₂", "<sub>2</sub>")
                     .replaceAll("²", "<sup>2</sup>")
                     .replaceAll("³", "<sup>3</sup>")
                     .replaceAll("½", "&frac12;")
                     .replaceAll("¼", "&frac14;")
                     .replaceAll("¾", "&frac34;");
        
        // 处理分数格式
        input = input.replaceAll("([0-9]+)/([0-9]+)", "<sup>$1</sup>&frasl;<sub>$2</sub>");
        
        return input;
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    private String toMarkdown(int id, String questionText) {
        String safe = escapeMarkdownText(questionText);
        String[] lines = safe.split("\n");
        StringBuilder stem = new StringBuilder();
        List<String> options = new ArrayList<>();

        Pattern optionPattern = Pattern.compile("^\\s*([A-D])[\\.．]\\s*(.*)$");
        for (String line : lines) {
            String l = line == null ? "" : line.trim();
            if (l.isEmpty()) {
                continue;
            }
            Matcher m = optionPattern.matcher(l);
            if (m.find()) {
                String opt = m.group(1) + ". " + m.group(2);
                options.add(opt.trim());
            } else {
                if (stem.length() > 0) {
                    stem.append("\n");
                }
                stem.append(l);
            }
        }

        StringBuilder md = new StringBuilder();
        md.append(id).append(". ").append(stem);
        if (!options.isEmpty()) {
            md.append("\n\n");
            for (String opt : options) {
                md.append("- ").append(opt).append("\n");
            }
        }
        return md.toString().trim();
    }

    private String escapeMarkdownText(String input) {
        if (input == null) {
            return "";
        }
        // 防止前端 markdown 渲染时插入原始 HTML
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        // 先统一换行符为实际的换行符
        String normalized = content.replace("\r\n", "\n").replace("\r", "\n");
        // 合并多余空白，但保留换行
        normalized = normalized.replaceAll("[\\t\\x0B\\f]+", " ");
        normalized = normalized.replaceAll(" +", " ");
        // 在题号前插入换行（更宽松的规则，支持PDF格式）
        // 匹配：数字 + 点号/顿号/括号 + 空格 + 非数字内容
        normalized = normalized.replaceAll("(?m)(?<!\n)(?=\\d+[\\.．、\\)]\\s*[^\\d\\s])", "\n");
        // 匹配带圈数字
        normalized = normalized.replaceAll("(?m)(?<!\n)(?=[①②③④⑤⑥⑦⑧⑨⑩][\\.．、\\)]\\s*)", "\n");
        // 匹配"第X题"格式
        normalized = normalized.replaceAll("(?m)(?<!\n)(?=第\\s*[一二三四五六七八九十\\d]+\\s*[题道])", "\n");
        // 在选项前插入换行（如果选项不在行首）
        normalized = normalized.replaceAll("(?m)(?<!\n)(?<!^)(?=[A-D][\\.．、\\)])", "\n");
        // 清理连续空行
        normalized = normalized.replaceAll("\n{3,}", "\n\n");
        return normalized.trim();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 获取文件路径
     */
    private String getFilePath(MultipartFile file) {
        // 返回文件存储路径
        return "/upload/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
    }

    /**
     * 转义JSON特殊字符
     */
    private String escapeJson(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * 自定义PDF文本提取器，保留更多格式信息
     */
    private static class CustomPDFTextStripper extends PDFTextStripper {
        public CustomPDFTextStripper() throws IOException {
            super();
        }
    }
}
