package com.ape.apeadmin.service.ai;

import com.ape.apesystem.domain.FunPhysicsMoments;
import com.ape.apesystem.domain.FunPhysicsMomentsComment;
import org.springframework.ai.chat.client.ChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class MomentsAiReplyService {

    private static final Logger log = LoggerFactory.getLogger(MomentsAiReplyService.class);

    @Autowired
    private ChatClient chatClient;

    @Value("classpath:prompts/moments_ai_reply.txt")
    private Resource momentsPrompt;

    public FunPhysicsMomentsComment generateAiReply(FunPhysicsMoments moments, String userComment, String userName) {
        try {
            log.info("开始生成AI回复: momentsId={}, physicist={}, userComment={}", 
                moments.getId(), moments.getPhysicistName(), userComment);
            
            String systemPrompt = loadPrompt();
            log.debug("System prompt loaded: {}", systemPrompt);
            
            String userPrompt = String.format(
                "物理学家：%s（%s）\n" +
                "朋友圈内容：%s\n" +
                "用户%s评论：%s\n\n" +
                "请以这位物理学家的身份，用幽默风趣的方式回复用户的评论。\n" +
                "回复格式要求：必须以\"回复@用户名\"开头，例如：回复@朗威 你的回复内容\n" +
                "回复要简短（不超过50字），符合物理学家的性格特点。",
                moments.getPhysicistName(),
                moments.getPhysicistTitle(),
                moments.getContent(),
                userName != null ? userName : "同学",
                userComment
            );
            log.debug("User prompt: {}", userPrompt);

            log.info("调用ChatClient生成AI回复...");
            String aiReply = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("AI原始回复: {}", aiReply);
            
            aiReply = aiReply.replaceAll("^[\"「]|[」\"]$", "").trim();
            if (aiReply.length() > 100) {
                aiReply = aiReply.substring(0, 100) + "...";
            }

            FunPhysicsMomentsComment comment = new FunPhysicsMomentsComment();
            comment.setId(UUID.randomUUID().toString());
            comment.setMomentsId(moments.getId());
            comment.setCommenterName(moments.getPhysicistName());
            comment.setCommenterAvatar(moments.getPhysicistAvatar());
            comment.setContent(aiReply);
            comment.setSortOrder(999);
            comment.setStatus(0);
            comment.setIsAiReply(1);
            comment.setCreateTime(new Date());

            log.info("AI回复生成成功: 物理学家={}, 内容={}", moments.getPhysicistName(), aiReply);
            return comment;

        } catch (Exception e) {
            log.error("AI自动回复生成失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private String loadPrompt() {
        try {
            return new String(momentsPrompt.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return getDefaultPrompt();
        }
    }

    private String getDefaultPrompt() {
        return """
            你是一位风趣幽默的物理学家，正在回复朋友圈评论。
            
            回复要求：
            1. 保持物理学家的专业性和幽默感
            2. 可以引用物理知识或科学典故
            3. 语言简洁，不超过50字
            4. 语气亲切友好，像朋友聊天
            5. 可以适当使用物理相关的表情或符号
            
            注意：直接输出回复内容，不要加引号或其他格式。
            """;
    }
}
