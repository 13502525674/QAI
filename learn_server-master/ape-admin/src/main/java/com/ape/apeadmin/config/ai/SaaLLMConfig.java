package com.ape.apeadmin.config.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class SaaLLMConfig {

    @Bean
    @Primary
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultFunctions("calculateFormula", "searchKnowledge", "checkProgress")
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * P1-10: 批改专用 ChatClient — qwen3.7-plus + temperature=0 保证评分一致性
     *
     * 用于 PhysicsTools.correctExamPaper 中的 AI 批改，
     * 确保同一份试卷多次批改分数一致。
     */
    @Bean("gradingChatClient")
    public ChatClient gradingChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen3.7-max")
                        .withTemperature(0.0)
                        .build())
                .build();
    }

    /**
     * AI 推荐专用 ChatClient — 深度推理
     *
     * 用于个性化推荐，需要结合学生画像深度分析。
     */
    @Bean("recommendChatClient")
    public ChatClient recommendChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen3.7-max")
                        .withTemperature(0.5)
                        .build())
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * 创意类 ChatClient — 发散思维
     *
     * 用于朋友圈 AI 回复、AI 画室等需要创意发散的场景。
     */
    @Bean("creativeChatClient")
    public ChatClient creativeChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen3.7-max")
                        .withTemperature(0.8)
                        .build())
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Bean
    public ChatMemory chatMemory(StringRedisTemplate redisTemplate) {
        return new RedisChatMemory(redisTemplate);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }
}
