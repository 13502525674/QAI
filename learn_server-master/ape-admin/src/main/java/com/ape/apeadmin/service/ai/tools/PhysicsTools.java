package com.ape.apeadmin.service.ai.tools;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apeadmin.service.ai.RAGService;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.*;
import com.ape.apesystem.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class PhysicsTools {

    public record CalculatorRequest(String expression) {}
    public record CalculatorResponse(String result) {}

    @Bean
    @Description("Calculate mathematical expressions or physics formulas")
    public Function<CalculatorRequest, CalculatorResponse> calculateFormula() {
        return request -> {
            try {
                double result = cn.hutool.core.math.Calculator.conversion(request.expression());
                return new CalculatorResponse(String.valueOf(result));
            } catch (Exception e) {
                return new CalculatorResponse("Calculation error: " + e.getMessage());
            }
        };
    }

    public record SearchRequest(String query) {}
    public record SearchResponse(String content) {}
    
    @Bean
    @Description("Search for physics concepts in the knowledge base")
    public Function<SearchRequest, SearchResponse> searchKnowledge(RAGService ragService) {
        return request -> {
             List<String> results = ragService.retrieve(request.query());
             String content = String.join("\n\n", results);
             if (content.isEmpty()) {
                 return new SearchResponse("No relevant information found in knowledge base.");
             }
             return new SearchResponse("Found relevant info:\n" + content);
        };
    }

    public record ProgressRequest(String studentId) {}
    public record ProgressResponse(String status) {}

    @Bean
    @Description("Check student learning progress")
    public Function<ProgressRequest, ProgressResponse> checkProgress() {
        return request -> {
            // Mock database lookup
            return new ProgressResponse("Student " + request.studentId() + " has completed 3/5 mechanics modules.");
        };
    }

    public record CreateQuestionPaperRequest(String paperTitle, String subjectBranch, List<PaperQuestion> questions) {}
    public record PaperQuestion(Integer type, String title, String content, List<Object> options, Object answer, String explanation, Integer maxScore, Integer difficulty) {}
    public record CreateQuestionPaperResponse(String result, String paperId) {}

    @Bean
    @Description("Create a physics question paper for the question bank. Required: paperTitle, subjectBranch (kinematics/mechanics/electrics/optics/thermodynamics), questions with type (0/1/2/3), title, options (for choice), answer.")
    public Function<CreateQuestionPaperRequest, CreateQuestionPaperResponse> createPhysicsQuestionPaper(PhysicsQuestionPaperService physicsQuestionPaperService) {
        return request -> {
            if (request == null) {
                return new CreateQuestionPaperResponse("请求为空，无法创建试卷。", null);
            }
            String paperTitle = request.paperTitle();
            if (paperTitle == null || paperTitle.trim().isEmpty()) {
                return new CreateQuestionPaperResponse("试卷标题不能为空。", null);
            }
            if (paperTitle.length() > 200) {
                return new CreateQuestionPaperResponse("试卷标题长度不能超过200个字符。", null);
            }
            String subjectBranch = request.subjectBranch();
            if (subjectBranch == null || subjectBranch.trim().isEmpty()) {
                return new CreateQuestionPaperResponse("物理分支不能为空。", null);
            }
            String normalizedBranch = normalizeBranch(subjectBranch);
            if (normalizedBranch == null) {
                return new CreateQuestionPaperResponse("物理分支不合法，请使用：kinematics/mechanics/electrics/optics/thermodynamics。", null);
            }
            List<PaperQuestion> questions = request.questions();
            if (questions == null || questions.isEmpty()) {
                return new CreateQuestionPaperResponse("题目列表不能为空。", null);
            }

            List<Map<String, Object>> normalizedQuestions = new ArrayList<>();
            int choiceCount = 0;
            int fillCount = 0;
            int calculationCount = 0;
            int trueFalseCount = 0;

            for (PaperQuestion question : questions) {
                Map<String, Object> normalized = normalizeQuestion(question);
                if (normalized == null) {
                    continue;
                }
                normalizedQuestions.add(normalized);
                int type = ((Number) normalized.getOrDefault("type", 0)).intValue();
                if (type == 0) choiceCount++;
                if (type == 1) fillCount++;
                if (type == 2) calculationCount++;
                if (type == 3) trueFalseCount++;
            }

            if (normalizedQuestions.isEmpty()) {
                return new CreateQuestionPaperResponse("题目内容为空或格式不正确。", null);
            }
            int minQuestions = 10;
            if (normalizedQuestions.size() < minQuestions) {
                return new CreateQuestionPaperResponse("题量不足，请至少生成 " + minQuestions + " 题。", null);
            }
            List<String> missingTypes = new ArrayList<>();
            if (choiceCount == 0) missingTypes.add("选择题");
            if (fillCount == 0) missingTypes.add("填空题");
            if (calculationCount == 0) missingTypes.add("计算题");
            if (trueFalseCount == 0) missingTypes.add("判断题");
            if (!missingTypes.isEmpty()) {
                return new CreateQuestionPaperResponse("题型缺失：" + String.join("、", missingTypes) + "。", null);
            }

            normalizedQuestions.sort(Comparator.comparingInt(q -> getTypeOrder(q.get("type"))));

            PhysicsQuestionPaper paper = new PhysicsQuestionPaper()
                    .setPaperTitle(paperTitle.trim())
                    .setSubjectBranch(normalizedBranch)
                    .setQuestionContent(JSON.toJSONString(normalizedQuestions))
                    .setTotalQuestions(normalizedQuestions.size())
                    .setChoiceQuestionsCount(choiceCount)
                    .setFillInQuestionsCount(fillCount)
                    .setCalculationQuestionsCount(calculationCount);
            paper.setCreateBy("布莱克");

            ApeUser user = ShiroUtils.getUserInfo();
            if (user != null) {
                paper.setCreatorId(user.getId());
            }

            boolean saved = physicsQuestionPaperService.save(paper);
            if (saved) {
                return new CreateQuestionPaperResponse("试卷已保存。", paper.getId());
            }
            return new CreateQuestionPaperResponse("试卷保存失败。", null);
        };
    }

    // --- New Grading Tool ---
    
    public record CorrectRequest(String studentName, String testName) {}
    public record CorrectResponse(String result) {}

    @Bean
    @Description("Help teachers grade student exam papers and provide AI analysis. Requires student name. If test name is not specified, leave it empty to find ungraded papers.")
    public Function<CorrectRequest, CorrectResponse> correctExamPaper(
            ApeTestStudentService apeTestStudentService,
            ApeTestService apeTestService,
            PhysicsPracticeRecordsService practiceRecordsService,
            PhysicsQuestionPaperService questionPaperService,
            ApeUserService userService,
            @org.springframework.beans.factory.annotation.Qualifier("gradingChatClient") ChatClient chatClient
    ) {
        return request -> {
            try {
                // 0. Validate Input
                if (request.studentName() == null || request.studentName().trim().isEmpty()) {
                    return new CorrectResponse("无法执行批改：请提供具体的【学生姓名】。");
                }

                // 1. Find Student
                ApeUser student = userService.getOne(new QueryWrapper<ApeUser>().like("user_name", request.studentName()).last("limit 1"));
                if (student == null) {
                    return new CorrectResponse("未找到名为 " + request.studentName() + " 的学生。");
                }

                // --- SYSTEM 1: Physics Practice Records (Priority) ---
                PhysicsPracticeRecords record = null;
                PhysicsQuestionPaper paper = null;
                String inputTestName = request.testName();
                boolean isUngradedRequest = inputTestName != null && (inputTestName.contains("未批改") || inputTestName.contains("待批改"));

                // Strategy A: Find by Paper Name (if provided)
                if (!isUngradedRequest && inputTestName != null && !inputTestName.isEmpty()) {
                    // 1. Try direct search on PhysicsPracticeRecords (using new paper_name field)
                    try {
                        List<PhysicsPracticeRecords> records = practiceRecordsService.list(new QueryWrapper<PhysicsPracticeRecords>()
                                .eq("user_id", student.getId())
                                .like("paper_name", inputTestName)
                                .orderByDesc("submitted_at"));
                        
                        if (!records.isEmpty()) {
                            record = records.get(0);
                            paper = questionPaperService.getById(record.getPaperId());
                        }
                    } catch (Exception e) {
                        // Ignore exception if column doesn't exist yet, fallback to Strategy B
                        System.out.println("Strategy A failed (likely missing column): " + e.getMessage());
                    }

                    // 2. Fallback: Find Paper by Title in PhysicsQuestionPaper and join
                    if (record == null) {
                        List<PhysicsQuestionPaper> papers = questionPaperService.list(new QueryWrapper<PhysicsQuestionPaper>()
                                .like("paper_title", inputTestName));
                        
                        if (!papers.isEmpty()) {
                            // Find the one that the user has actually submitted
                            for (PhysicsQuestionPaper p : papers) {
                                PhysicsPracticeRecords r = practiceRecordsService.getOne(new QueryWrapper<PhysicsPracticeRecords>()
                                        .eq("user_id", student.getId())
                                        .eq("paper_id", p.getId())
                                        .orderByDesc("submitted_at")
                                        .last("limit 1"));
                                if (r != null) {
                                    paper = p;
                                    record = r;
                                    break;
                                }
                            }
                        }
                    }
                }

                // Strategy B: Find Ungraded (if no specific test found yet)
                if (record == null) {
                    List<PhysicsPracticeRecords> ungradedRecords = practiceRecordsService.list(new QueryWrapper<PhysicsPracticeRecords>()
                            .eq("user_id", student.getId())
                            .ne("status", "graded") // Assuming 'graded' is the status for completed grading
                            .orderByDesc("submitted_at"));
                    
                    if (!ungradedRecords.isEmpty()) {
                        // If specific name was requested but not found in Strategy A, try to match here?
                        // For now, just pick the latest ungraded one if name was not specific or failed
                        if (inputTestName == null || inputTestName.isEmpty() || isUngradedRequest) {
                            record = ungradedRecords.get(0);
                            paper = questionPaperService.getById(record.getPaperId());
                        } else {
                            // Try to match name from ungraded list
                            for (PhysicsPracticeRecords r : ungradedRecords) {
                                PhysicsQuestionPaper p = questionPaperService.getById(r.getPaperId());
                                if (p != null && (p.getPaperTitle().contains(inputTestName) || inputTestName.contains(p.getPaperTitle()))) {
                                    record = r;
                                    paper = p;
                                    break;
                                }
                            }
                        }
                    }
                }

                // Execute Grading for Physics System
                if (record != null && paper != null) {
                    // Check if already graded (unless "重新" is in the request, though request only has testName)
                    // If user explicitly asks to regrade, we might need a flag. For now, just block if graded.
                    if ("graded".equals(record.getStatus())) {
                        return new CorrectResponse("试卷 [" + paper.getPaperTitle() + "] 已经由 " + (record.getGradedBy() != null ? record.getGradedBy() : "老师") + " 批改完成。总分：" + record.getScore() + "。如果您需要重新批改，请联系管理员重置状态。");
                    }
                    return gradePhysicsPractice(record, paper, student, chatClient, practiceRecordsService);
                }


                // --- SYSTEM 2: Legacy ApeTest System (Fallback) ---
                // (Existing logic preserved as fallback)
                
                ApeTest test = null;
                
                // Case A: Try to find specific test
                if (!isUngradedRequest && inputTestName != null && !inputTestName.isEmpty()) {
                    test = apeTestService.getOne(new QueryWrapper<ApeTest>().eq("name", inputTestName).last("limit 1"));
                    if (test == null) {
                         test = apeTestService.getOne(new QueryWrapper<ApeTest>().like("name", inputTestName).last("limit 1"));
                    }
                }
                
                // Case B: Fallback to finding ungraded
                if (test == null) {
                    List<ApeTestStudent> ungradedItems = apeTestStudentService.list(new QueryWrapper<ApeTestStudent>()
                        .select("distinct test_id")
                        .eq("user_id", student.getId())
                        .and(w -> w.isNull("graded_by").or().ne("graded_by", "TEACHER").ne("graded_by", "AI"))
                    );
                    
                    if (!ungradedItems.isEmpty()) {
                        String targetTestId = ungradedItems.get(0).getTestId();
                        test = apeTestService.getById(targetTestId);
                        
                        // Verify name match if provided
                        if (inputTestName != null && !inputTestName.isEmpty() && !isUngradedRequest) {
                             if (!test.getName().contains(inputTestName) && !inputTestName.contains(test.getName())) {
                                 test = null; // Name doesn't match
                             }
                        }
                    }
                }

                if (test != null) {
                     return gradeApeTest(test, student, chatClient, apeTestStudentService);
                }

                return new CorrectResponse("未找到名为 \"" + (inputTestName != null ? inputTestName : "未指定") + "\" 的试卷，也未发现其他待批改的试卷。");

            } catch (Exception e) {
                e.printStackTrace();
                return new CorrectResponse("批改过程中发生错误: " + e.getMessage());
            }
        };
    }

    // Helper for Physics Practice System Grading
    private CorrectResponse gradePhysicsPractice(PhysicsPracticeRecords record, PhysicsQuestionPaper paper, ApeUser student, ChatClient chatClient, PhysicsPracticeRecordsService practiceRecordsService) {
        try {
            JSONArray questions = JSON.parseArray(paper.getQuestionContent());
            // Answers might be in a separate JSON or map. Let's assume standard format for now or extract from questions if embedded.
            // If answerContent is separate:
            // JSONArray answers = JSON.parseArray(paper.getAnswerContent()); 
            
            // User answers: handle both Map (Object) and List (Array) formats
            Map<String, Object> userAnswersMap = new HashMap<>();
            String answersStr = record.getUserAnswers();
            
            if (answersStr != null && !answersStr.trim().isEmpty()) {
                try {
                    String trimmed = answersStr.trim();
                    if (trimmed.startsWith("{")) {
                        userAnswersMap = JSON.parseObject(trimmed);
                    } else if (trimmed.startsWith("[")) {
                        JSONArray arr = JSON.parseArray(trimmed);
                        for (int k = 0; k < arr.size(); k++) {
                            Object item = arr.get(k);
                            // If item is an object with key/value structure
                            if (item instanceof JSONObject) {
                                JSONObject jo = (JSONObject) item;
                                // Try common keys for question ID
                                String key = jo.getString("questionNumber");
                                if (key == null) key = jo.getString("id");
                                if (key == null) key = jo.getString("questionId");
                                // Fallback to index if no ID found
                                if (key == null) key = String.valueOf(k + 1);
                                
                                // Try common keys for answer value
                                Object val = jo.get("answer");
                                if (val == null) val = jo.get("value");
                                if (val == null) val = jo.get("userAnswer");
                                
                                userAnswersMap.put(key, val);
                            } else {
                                // Simple value (string/int), map to 1-based index
                                userAnswersMap.put(String.valueOf(k + 1), item);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing user answers: " + e.getMessage());
                }
            }
            
            List<Map<String, Object>> gradingResults = new ArrayList<>();
            int totalScore = 0;
            int maxScore = 0;

            for (int i = 0; i < questions.size(); i++) {
                JSONObject q = questions.getJSONObject(i);
                String qId = q.getString("questionNumber"); // or "id"
                if (qId == null) qId = String.valueOf(i + 1);
                
                String type = q.getString("type");
                String title = q.getString("title");
                String standardAnswer = q.getString("answerLabel");
                if (standardAnswer == null || standardAnswer.trim().isEmpty()) {
                    Object answerObj = q.get("answer");
                    standardAnswer = answerObj != null ? String.valueOf(answerObj) : null;
                    if (standardAnswer != null) {
                        String normalizedLabel = normalizeChoiceAnswerLabel(answerObj, getOptionsSize(q));
                        if (normalizedLabel != null) {
                            standardAnswer = normalizedLabel;
                        }
                    }
                }
                // If not, try looking up in answerContent
                
                String userAnswer = userAnswersMap != null ? (String) userAnswersMap.get(qId) : "";
                
                // Determine max score based on question type (aligned with frontend logic)
                // type: 0=选择题, 1=填空题, 2=计算题, 3=判断题, 4=简答题
                int qScore = 10; // Default
                if (type != null) {
                    try {
                        int typeInt = Integer.parseInt(type);
                        switch (typeInt) {
                            case 0: qScore = 5; break;  // 选择题
                            case 1: qScore = 8; break;  // 填空题
                            case 2: qScore = 10; break; // 计算题
                            case 3: qScore = 5; break;  // 判断题
                            case 4: qScore = 10; break; // 简答题
                            default: qScore = 10; break;
                        }
                    } catch (NumberFormatException e) {
                        // If type is not a number, keep default 10
                    }
                }
                
                // Override if specific maxScore is present in question data
                if (q.containsKey("maxScore") && q.getIntValue("maxScore") > 0) {
                    qScore = q.getIntValue("maxScore");
                }
                
                maxScore += qScore;

                // AI Grading
                String prompt = String.format("""
                    你是一位物理老师。请批改这道题。
                    题目：%s
                    类型：%s
                    标准答案：%s
                    学生回答：%s
                    本题满分：%d
                    
                    请严格评分并给出解析。如果是客观题，只需比对答案。如果是主观题，请根据关键词和语义评分。
                    请直接返回 JSON 字符串，**不要**包含 Markdown 代码块（如 ```json），**不要**包含任何其他前缀或后缀文本。
                    格式如下：
                    {
                      "score": <整数得分>,
                      "analysis": "<简短解析，不要使用Markdown格式，不要包含双引号等特殊字符，如有必要请使用中文引号>"
                    }
                    """,
                    title, type, standardAnswer, userAnswer, qScore
                );

                try {
                    // P1-10: 一致性保障 — temperature=0 确保评分确定性
                    String aiResponse = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();
                    
                    // Robust JSON Extraction
                    int startIndex = aiResponse.indexOf("{");
                    int endIndex = aiResponse.lastIndexOf("}");
                    
                    if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                        aiResponse = aiResponse.substring(startIndex, endIndex + 1);
                    } else {
                        // Fallback: try to clean markdown if extraction failed
                        aiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
                    }
                    
                    // Common LLM JSON fix: remove invalid escapes for braces
                    aiResponse = aiResponse.replace("\\{", "{").replace("\\}", "}");
                    
                    JSONObject json = JSONObject.parseObject(aiResponse);
                    
                    int score = json.getIntValue("score");
                    String analysis = json.getString("analysis");
                    
                    Map<String, Object> resultItem = new HashMap<>();
                    resultItem.put("questionId", qId);
                    resultItem.put("score", score);
                    resultItem.put("maxScore", qScore);
                    resultItem.put("analysis", analysis);
                    resultItem.put("userAnswer", userAnswer);
                    resultItem.put("standardAnswer", standardAnswer);
                    
                    gradingResults.add(resultItem);
                    totalScore += score;
                    
                } catch (Exception e) {
                    System.err.println("AI grading failed for Q" + qId + ": " + e.getMessage());
                    // AI评分失败时，添加默认评分结果（0分）
                    Map<String, Object> resultItem = new HashMap<>();
                    resultItem.put("questionId", qId);
                    resultItem.put("score", 0);
                    resultItem.put("maxScore", qScore);
                    resultItem.put("analysis", "AI评分失败，请教师手动评分");
                    resultItem.put("userAnswer", userAnswer);
                    resultItem.put("standardAnswer", standardAnswer);
                    gradingResults.add(resultItem);
                }
            }
            
            // Update Record - 将评分结果包装为对象格式，包含questions数组
            Map<String, Object> gradingDetailsMap = new HashMap<>();
            gradingDetailsMap.put("questions", gradingResults);
            
            record.setScore((double) totalScore);
            record.setStatus("graded");
            record.setGradedBy("AI");
            record.setGradedAt(new Date());
            record.setGradingDetails(JSON.toJSONString(gradingDetailsMap));
            
            practiceRecordsService.updateById(record);
            
            return new CorrectResponse("已为您自动定位并批改物理练习: [" + paper.getPaperTitle() + "]。\n\nAI 批改完成。学生 " + student.getUserName() + " 得分: " + totalScore + "/" + maxScore);

        } catch (Exception e) {
            e.printStackTrace();
            return new CorrectResponse("物理练习批改失败: " + e.getMessage());
        }
    }

    // Helper for Legacy ApeTest System Grading
    private CorrectResponse gradeApeTest(ApeTest test, ApeUser student, ChatClient chatClient, ApeTestStudentService testStudentService) {
         final String currentTestName = test.getName();

        // 3. Get Student Answers
        List<ApeTestStudent> answerList = testStudentService.list(new QueryWrapper<ApeTestStudent>()
                .eq("user_id", student.getId())
                .eq("test_id", test.getId()));

        if (answerList == null || answerList.isEmpty()) {
            return new CorrectResponse("该学生未参加该考试或未提交答案。");
        }

        int totalScore = 0;
        int maxScore = 0;
        boolean anyGraded = false;

        for (ApeTestStudent item : answerList) {
            maxScore += item.getScore();
            
            // Skip if already graded by Teacher
            if (item.getPoint() != null && "TEACHER".equals(item.getGradedBy())) {
                totalScore += item.getPoint();
                continue;
            }

            // AI Grading Logic
            String prompt = String.format("""
                你是一位物理老师。请批改这道题。
                题目：%s
                类型：%d (0:单选, 1:多选, 2:填空, 3:判断, 4:问答, 5:计算)
                标准答案：%s
                学生回答：%s
                本题满分：%d
                得分点关键词：%s
                
                请严格评分并给出解析。如果是客观题，只需比对答案。如果是主观题，请根据关键词和语义评分。
                请输出纯净的 JSON 格式（不要Markdown代码块）：
                {
                  "score": <整数得分>,
                  "analysis": "<简短解析>"
                }
                """,
                item.getTitle(),
                item.getType(),
                item.getAnswer(),
                item.getSolution(),
                item.getScore(),
                item.getKeyword()
            );

            try {
                String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
                // Clean markdown if present
                aiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
                
                JSONObject json = JSONObject.parseObject(aiResponse);
                int score = json.getIntValue("score");
                String analysis = json.getString("analysis");

                item.setPoint(score);
                item.setAiAnalysis(analysis);
                item.setGradedBy("AI");
                totalScore += score;
                anyGraded = true;
                
            } catch (Exception e) {
                // If AI fails, fallback to 0 or keep null
                System.err.println("AI grading failed for item " + item.getId() + ": " + e.getMessage());
                item.setAiAnalysis("AI 批改暂时不可用");
                item.setGradedBy("AI_ERROR");
            }
        }

        // 5. Save updates
        if (anyGraded) {
            testStudentService.updateBatchById(answerList);
            return new CorrectResponse("已为您自动定位并批改试卷: [" + currentTestName + "]。\n\nAI 批改完成。学生 " + student.getUserName() + " 在该试卷中的得分为: " + totalScore + "/" + maxScore);
        } else {
            return new CorrectResponse("试卷 [" + currentTestName + "] 已由老师批改完成，无需 AI 再次批改。当前得分: " + totalScore);
        }
    }

    private String normalizeBranch(String branch) {
        String value = branch.trim();
        if ("kinematics".equalsIgnoreCase(value) || "运动学".equals(value)) return "kinematics";
        if ("mechanics".equalsIgnoreCase(value) || "力学".equals(value)) return "mechanics";
        if ("electrics".equalsIgnoreCase(value) || "电学".equals(value)) return "electrics";
        if ("optics".equalsIgnoreCase(value) || "光学".equals(value)) return "optics";
        if ("thermodynamics".equalsIgnoreCase(value) || "热学".equals(value)) return "thermodynamics";
        return null;
    }

    private Map<String, Object> normalizeQuestion(PaperQuestion question) {
        if (question == null) {
            return null;
        }
        String rawTitle = question.title();
        String rawContent = question.content();
        List<Map<String, Object>> options = normalizeOptions(question.options());
        if (options.isEmpty() && !isBlank(rawContent)) {
            options = extractOptionsFromContent(rawContent);
        }
        int type = normalizeQuestionType(question.type(), rawTitle, rawContent, options);
        String title = !isBlank(rawTitle) ? rawTitle : cleanTitleFromContent(rawContent);
        if (isBlank(title)) {
            return null;
        }

        Map<String, Object> normalized = new HashMap<>();
        normalized.put("type", type);
        normalized.put("title", title);

        if (!isBlank(question.explanation())) {
            normalized.put("explanation", question.explanation());
        }
        
        // 根据题型设置默认满分，如果AI指定了maxScore则使用指定值
        int defaultMaxScore;
        switch (type) {
            case 0: defaultMaxScore = 5; break;  // 选择题
            case 1: defaultMaxScore = 8; break;  // 填空题
            case 2: defaultMaxScore = 10; break; // 计算题
            case 3: defaultMaxScore = 5; break;  // 判断题
            case 4: defaultMaxScore = 10; break; // 简答题
            default: defaultMaxScore = 10; break;
        }
        if (question.maxScore() != null && question.maxScore() > 0) {
            normalized.put("maxScore", question.maxScore());
        } else {
            normalized.put("maxScore", defaultMaxScore);
        }
        
        if (question.difficulty() != null) {
            normalized.put("difficulty", question.difficulty());
        }

        Object answer = question.answer();

        if (type == 0) {
            if (options.isEmpty()) {
                return null;
            }
            normalized.put("options", options);
            Integer answerIndex = normalizeChoiceAnswerIndex(answer, options.size());
            String answerLabel = normalizeChoiceAnswerLabel(answer, options.size());
            if (answerIndex != null) {
                normalized.put("answer", answerIndex);
            } else if (!isBlank(answerLabel)) {
                normalized.put("answer", answerLabel);
            }
            if (!isBlank(answerLabel)) {
                normalized.put("answerLabel", answerLabel);
            }
        } else if (type == 3) {
            Boolean tf = normalizeTrueFalseAnswer(answer);
            if (tf != null) {
                normalized.put("answer", tf);
                normalized.put("answerLabel", tf ? "正确" : "错误");
            }
        } else {
            if (answer != null && !isBlank(String.valueOf(answer))) {
                normalized.put("answer", String.valueOf(answer));
            }
        }

        return normalized;
    }

    private List<Map<String, Object>> normalizeOptions(List<Object> options) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (options == null) {
            return list;
        }
        for (Object option : options) {
            if (option == null) {
                continue;
            }
            String text;
            if (option instanceof Map) {
                Object textObj = ((Map<?, ?>) option).get("text");
                if (textObj == null) {
                    textObj = ((Map<?, ?>) option).get("option");
                }
                text = textObj != null ? String.valueOf(textObj) : null;
            } else {
                text = String.valueOf(option);
            }
            if (!isBlank(text)) {
                Map<String, Object> opt = new HashMap<>();
                opt.put("text", text);
                list.add(opt);
            }
        }
        return list;
    }

    private Integer normalizeChoiceAnswerIndex(Object answer, int optionSize) {
        if (answer == null) {
            return null;
        }
        Integer index = null;
        if (answer instanceof Number) {
            index = ((Number) answer).intValue();
        } else {
            String val = String.valueOf(answer).trim();
            if (val.matches("^[A-Za-z]$")) {
                index = Character.toUpperCase(val.charAt(0)) - 'A';
            } else {
                try {
                    index = Integer.parseInt(val);
                } catch (NumberFormatException ignored) {
                    index = null;
                }
            }
        }
        if (index == null || index < 0) {
            return null;
        }
        if (optionSize > 0 && index >= optionSize) {
            return null;
        }
        return index;
    }

    private String normalizeChoiceAnswerLabel(Object answer, int optionSize) {
        Integer index = normalizeChoiceAnswerIndex(answer, optionSize);
        if (index != null) {
            return String.valueOf((char) ('A' + index));
        }
        if (answer == null) {
            return null;
        }
        String val = String.valueOf(answer).trim();
        if (val.matches("^[A-Za-z]$")) {
            return val.toUpperCase();
        }
        return null;
    }

    private Boolean normalizeTrueFalseAnswer(Object answer) {
        if (answer == null) {
            return null;
        }
        if (answer instanceof Boolean) {
            return (Boolean) answer;
        }
        String val = String.valueOf(answer).trim();
        if ("true".equalsIgnoreCase(val) || "正确".equals(val) || "对".equals(val)) {
            return true;
        }
        if ("false".equalsIgnoreCase(val) || "错误".equals(val) || "错".equals(val)) {
            return false;
        }
        return null;
    }

    private int getOptionsSize(JSONObject question) {
        JSONArray options = question.getJSONArray("options");
        return options == null ? 0 : options.size();
    }

    private int getTypeOrder(Object type) {
        int value;
        if (type instanceof Number) {
            value = ((Number) type).intValue();
        } else {
            try {
                value = Integer.parseInt(String.valueOf(type));
            } catch (NumberFormatException ex) {
                value = -1;
            }
        }
        if (value == 0) return 0;
        if (value == 1) return 1;
        if (value == 3) return 2;
        if (value == 2) return 3;
        return 4;
    }

    private int normalizeQuestionType(Integer inputType, String title, String content, List<Map<String, Object>> options) {
        int type = inputType == null ? -1 : inputType;
        if (type < 0 || type > 3) {
            type = -1;
        }
        if (type == -1) {
            if (options != null && !options.isEmpty()) {
                return 0;
            }
            String text = mergeText(title, content);
            if (isBlank(text)) {
                return 2;
            }
            if (containsBlank(text)) {
                return 1;
            }
            if (containsTrueFalse(text)) {
                return 3;
            }
            if (containsCalculation(text)) {
                return 2;
            }
            return 2;
        }
        if (type == 0 && (options == null || options.isEmpty())) {
            return 0;
        }
        return type;
    }

    private List<Map<String, Object>> extractOptionsFromContent(String content) {
        List<Map<String, Object>> options = new ArrayList<>();
        if (isBlank(content)) {
            return options;
        }
        String[] lines = content.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^[A-Z][\\.．、\\)]\\s*.+")) {
                String text = trimmed.replaceFirst("^[A-Z][\\.．、\\)]\\s*", "");
                if (!isBlank(text)) {
                    Map<String, Object> option = new HashMap<>();
                    option.put("text", text);
                    options.add(option);
                }
            }
        }
        return options;
    }

    private String cleanTitleFromContent(String content) {
        if (isBlank(content)) {
            return null;
        }
        String cleaned = content.replaceAll("(?m)^\\s*[A-Z][\\.．、\\)].*$", "");
        cleaned = cleaned.replaceAll("\\n{2,}", "\n").trim();
        return cleaned;
    }

    private String mergeText(String title, String content) {
        if (!isBlank(title) && !isBlank(content)) {
            return title + "\n" + content;
        }
        if (!isBlank(title)) return title;
        if (!isBlank(content)) return content;
        return null;
    }

    private boolean containsBlank(String text) {
        return text.contains("____") || text.matches(".*_{2,}.*") || text.matches(".*（\\s*）.*") || text.matches(".*（\\s*.{0,3}\\s*）.*");
    }

    private boolean containsCalculation(String text) {
        return text.contains("求：") || text.contains("求 ") || text.contains("计算") || text.contains("推导") || text.contains("证明") || text.contains("解答");
    }

    private boolean containsTrueFalse(String text) {
        return text.contains("判断") || text.contains("是否") || text.contains("正确") || text.contains("错误") || text.contains("对错");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
