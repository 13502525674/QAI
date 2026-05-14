package com.ape.apeadmin.service.ai.impl;

import com.ape.apeadmin.service.ai.AiAssistantService;
import com.ape.apesystem.domain.KnowledgePoint;
import com.ape.apesystem.domain.StudentMastery;
import com.ape.apesystem.domain.PhysicsQuestionPaper;
import com.ape.apesystem.domain.ApeTask;
import com.ape.apesystem.service.KnowledgePointService;
import com.ape.apesystem.service.StudentMasteryService;
import com.ape.apesystem.service.PhysicsQuestionPaperService;
import com.ape.apesystem.service.ApeTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    @Value("classpath:prompts/system_physics_tutor.txt")
    private Resource systemPrompt;

    @Autowired
    private StudentMasteryService studentMasteryService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private PhysicsQuestionPaperService physicsQuestionPaperService;

    @Autowired
    private ApeTaskService apeTaskService;

    public AiAssistantServiceImpl(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    @Override
    public List<Message> getHistory(String conversationId) {
        return chatMemory.get(conversationId, 1000);
    }

    @Override
    public Flux<String> chatStream(String message, String conversationId) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .functions("calculateFormula", "searchKnowledge", "checkProgress", "correctExamPaper", "createPhysicsQuestionPaper")
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content()
                .retry(3);
    }

    @Override
    public String chat(String message, String conversationId) {
        int maxRetries = 3;
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return chatClient.prompt()
                        .system(systemPrompt)
                        .user(message)
                        .functions("calculateFormula", "searchKnowledge", "checkProgress", "correctExamPaper", "createPhysicsQuestionPaper")
                        .advisors(new MessageChatMemoryAdvisor(chatMemory))
                        .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                                .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                        .call()
                        .content();
            } catch (Exception e) {
                lastException = e;
                try {
                    Thread.sleep(500 * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Failed to call AI after " + maxRetries + " retries", lastException);
    }

    @Override
    public void clearHistory(String conversationId) {
        chatMemory.clear(conversationId);
    }

    @Override
    public String buildPromptWithStudentProfile(String studentId, String userMessage) {
        StringBuilder prompt = new StringBuilder();
        
        Map<String, Object> profile = getStudentProfileForAI(studentId);
        
        prompt.append("## 学生当前能力状态\n");
        prompt.append("- 整体能力值：").append(String.format("%.1f%%", profile.get("overallScore"))).append("\n");
        
        @SuppressWarnings("unchecked")
        Map<String, Float> branchMastery = (Map<String, Float>) profile.get("branchMastery");
        if (branchMastery != null && !branchMastery.isEmpty()) {
            prompt.append("- 各分支掌握度：");
            for (Map.Entry<String, Float> entry : branchMastery.entrySet()) {
                prompt.append(entry.getKey()).append("(").append(String.format("%.0f%%", entry.getValue() * 100)).append(") ");
            }
            prompt.append("\n");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> strongPoints = (List<Map<String, Object>>) profile.get("strongPoints");
        if (strongPoints != null && !strongPoints.isEmpty()) {
            prompt.append("- 优势知识点：");
            for (Map<String, Object> sp : strongPoints) {
                prompt.append(sp.get("name")).append("(").append(String.format("%.0f%%", (Float) sp.get("mastery") * 100)).append(") ");
            }
            prompt.append("\n");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> weakPoints = (List<Map<String, Object>>) profile.get("weakPoints");
        if (weakPoints != null && !weakPoints.isEmpty()) {
            prompt.append("- 薄弱知识点：");
            for (Map<String, Object> wp : weakPoints) {
                prompt.append(wp.get("name")).append("(").append(String.format("%.0f%%", (Float) wp.get("mastery") * 100)).append(") ");
            }
            prompt.append("\n");
        }
        
        prompt.append("\n## 平台可用资源\n");
        
        prompt.append("### 可用试卷/题库：\n");
        List<PhysicsQuestionPaper> papers = physicsQuestionPaperService.list();
        if (papers != null && !papers.isEmpty()) {
            for (PhysicsQuestionPaper paper : papers) {
                prompt.append("- ").append(paper.getPaperTitle())
                      .append("（分支：").append(paper.getSubjectBranch() != null ? paper.getSubjectBranch() : "未分类")
                      .append("，题数：").append(paper.getTotalQuestions() != null ? paper.getTotalQuestions() : 0)
                      .append("）\n");
            }
        } else {
            prompt.append("暂无可用试卷\n");
        }
        
        prompt.append("\n### 可用课程：\n");
        QueryWrapper<ApeTask> taskWrapper = new QueryWrapper<>();
        taskWrapper.eq("state", 0);
        List<ApeTask> tasks = apeTaskService.list(taskWrapper);
        if (tasks != null && !tasks.isEmpty()) {
            for (ApeTask task : tasks) {
                prompt.append("- ").append(task.getName())
                      .append("（专业：").append(task.getMajor() != null ? task.getMajor() : "未分类")
                      .append("，分类：").append(task.getClassification() != null ? task.getClassification() : "未分类")
                      .append("，教师：").append(task.getTeacherName() != null ? task.getTeacherName() : "未知")
                      .append("）\n");
            }
        } else {
            prompt.append("暂无可用课程\n");
        }
        
        prompt.append("\n## 任务\n");
        prompt.append(userMessage).append("\n\n");
        prompt.append("请根据以上学生能力状态和平台可用资源，提供个性化的学习建议。推荐应该遵循：\n");
        prompt.append("1. 优先推荐平台已有的试卷和课程，明确给出试卷名称或课程名称\n");
        prompt.append("2. 根据学生薄弱分支推荐对应分支的试卷练习\n");
        prompt.append("3. 根据学生薄弱分支推荐对应专业的课程学习\n");
        prompt.append("4. 优先补足前置薄弱知识点\n");
        prompt.append("5. 难度匹配学生当前水平\n");
        prompt.append("6. 给出具体的学习路径建议\n");
        prompt.append("7. 推荐格式：推荐类型（试卷/课程）+ 具体名称 + 推荐理由\n");
        
        return prompt.toString();
    }

    @Override
    public Flux<String> personalizedRecommend(String studentId, String conversationId) {
        String prompt = buildPromptWithStudentProfile(studentId, 
            "请为该学生推荐最适合的学习资源，包括：\n" +
            "1. 推荐刷哪些试卷（从平台可用试卷中选择）\n" +
            "2. 推荐上哪些课程（从平台可用课程中选择）\n" +
            "3. 解释每个推荐的理由\n" +
            "请确保推荐内容来自平台已有资源，并给出具体的试卷名称或课程名称。");
        
        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .functions("calculateFormula", "searchKnowledge", "checkProgress", "correctExamPaper", "createPhysicsQuestionPaper")
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content()
                .retry(3);
    }

    @Override
    public Map<String, Object> getStudentProfileForAI(String studentId) {
        Map<String, Object> profile = new HashMap<>();
        
        Map<String, Float> branchMastery = studentMasteryService.getMasteryByBranch(studentId);
        profile.put("branchMastery", branchMastery);
        
        double overallScore = branchMastery.values().stream()
                .mapToDouble(val -> val != null ? val.doubleValue() : 0.0)
                .average()
                .orElse(0.0);
        profile.put("overallScore", overallScore * 100);
        
        List<StudentMastery> weakPointsList = studentMasteryService.getWeakPoints(studentId, 5);
        List<Map<String, Object>> weakPoints = new ArrayList<>();
        for (StudentMastery sm : weakPointsList) {
            Map<String, Object> point = new HashMap<>();
            KnowledgePoint kp = knowledgePointService.getById(sm.getKpId());
            if (kp != null) {
                point.put("id", kp.getId());
                point.put("name", kp.getName());
                point.put("branch", kp.getBranch());
                point.put("mastery", sm.getMasteryScore() != null ? sm.getMasteryScore() : 0f);
                weakPoints.add(point);
            }
        }
        profile.put("weakPoints", weakPoints);
        
        List<StudentMastery> strongPointsList = studentMasteryService.getStrongPoints(studentId, 3);
        List<Map<String, Object>> strongPoints = new ArrayList<>();
        for (StudentMastery sm : strongPointsList) {
            Map<String, Object> point = new HashMap<>();
            KnowledgePoint kp = knowledgePointService.getById(sm.getKpId());
            if (kp != null) {
                point.put("id", kp.getId());
                point.put("name", kp.getName());
                point.put("branch", kp.getBranch());
                point.put("mastery", sm.getMasteryScore() != null ? sm.getMasteryScore() : 0f);
                strongPoints.add(point);
            }
        }
        profile.put("strongPoints", strongPoints);
        
        return profile;
    }
}
