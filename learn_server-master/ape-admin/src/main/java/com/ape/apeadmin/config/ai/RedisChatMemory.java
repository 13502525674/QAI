package com.ape.apeadmin.config.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 对话记忆 — P1-9 优化
 *
 * 新增：
 * 1. 滑动窗口裁剪（MAX_TOKENS 限制，避免上下文超限）
 * 2. 早期消息 LLM 摘要压缩（保留关键信息）
 * 3. TTL 过期（7 天自动清理）
 * 4. Token 估算
 */
public class RedisChatMemory implements ChatMemory {

    private final StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    @Lazy
    private org.springframework.ai.chat.client.ChatClient chatClient;

    private static final String KEY_PREFIX = "ai:chat:memory:";
    private static final String SUMMARY_SUFFIX = ":summary";

    // P1-9: 滑动窗口限制
    private static final int MAX_MESSAGES = 100;       // 最多保留 100 条消息
    private static final int MAX_TOKENS = 30000;        // 最多 30k tokens（qwen 32k 上下文窗口）
    private static final long TTL_HOURS = 168;          // 7 天过期

    public RedisChatMemory(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = KEY_PREFIX + conversationId;
        List<String> jsonMessages = messages.stream()
                .map(this::serializeMessage)
                .collect(Collectors.toList());
        if (!jsonMessages.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, jsonMessages);

            // P1-9: 设置 TTL
            redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);

            // P1-9: 滑动窗口裁剪
            trimByTokens(key, MAX_TOKENS);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = KEY_PREFIX + conversationId;
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return new ArrayList<>();
        }

        long start = 0;
        if (lastN > 0 && size > lastN) {
            start = size - lastN;
        }

        List<String> jsonMessages = redisTemplate.opsForList().range(key, start, -1);
        if (jsonMessages == null) {
            return new ArrayList<>();
        }

        List<Message> messages = jsonMessages.stream()
                .map(this::deserializeMessage)
                .collect(Collectors.toList());

        // P1-9: 如果有早期摘要，注入为系统消息
        String summary = redisTemplate.opsForValue().get(key + SUMMARY_SUFFIX);
        if (summary != null && !summary.isEmpty()) {
            List<Message> result = new ArrayList<>();
            result.add(new SystemMessage("[历史对话摘要]\n" + summary));
            result.addAll(messages);
            return result;
        }

        return messages;
    }

    @Override
    public void clear(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        redisTemplate.delete(key);
        redisTemplate.delete(key + SUMMARY_SUFFIX);
    }

    /**
     * P1-9: 按 Token 数裁剪，超过限制时裁剪早期消息并生成摘要
     */
    private void trimByTokens(String key, int maxTokens) {
        List<String> all = redisTemplate.opsForList().range(key, 0, -1);
        if (all == null || all.isEmpty()) {
            return;
        }

        int totalTokens = estimateTokens(all);
        if (totalTokens <= maxTokens) {
            return;
        }

        // 从尾部保留，直到 Token 数满足
        int keepIndex = 0;
        int cumulative = 0;
        for (int i = all.size() - 1; i >= 0; i--) {
            cumulative += estimateTokens(all.get(i));
            if (cumulative > maxTokens) {
                keepIndex = i + 1;
                break;
            }
        }

        // 裁剪
        if (keepIndex > 0) {
            redisTemplate.opsForList().trim(key, keepIndex, -1);

            // 早期消息生成摘要（异步，避免阻塞）
            List<String> earlyMessages = all.subList(0, keepIndex);
            try {
                String summary = summarizeEarlyMessages(earlyMessages);
                if (summary != null) {
                    redisTemplate.opsForValue().set(
                        key + SUMMARY_SUFFIX, summary, TTL_HOURS, TimeUnit.HOURS
                    );
                }
            } catch (Exception e) {
                // 摘要失败不影响主流程
            }
        }

        // 硬限制消息条数
        Long currentSize = redisTemplate.opsForList().size(key);
        if (currentSize != null && currentSize > MAX_MESSAGES) {
            redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
        }
    }

    /**
     * 用 LLM 摘要早期对话（如果 ChatClient 可用）
     */
    private String summarizeEarlyMessages(List<String> earlyMessages) {
        if (chatClient == null) {
            // ChatClient 不可用时，简单截取前 500 字作为摘要
            String combined = earlyMessages.stream()
                    .limit(3)
                    .collect(Collectors.joining("\n"));
            return "[早期对话摘要-截取] " + combined.substring(0, Math.min(500, combined.length()));
        }

        try {
            String content = String.join("\n", earlyMessages);
            String prompt = "请将以下对话总结为关键信息，保留核心结论和重要数据，300 字以内：\n\n" + content;
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 粗略估算 Token 数：1 个中文字 ≈ 2 token，1 个英文单词 ≈ 1.3 token
     */
    private int estimateTokens(List<String> messages) {
        return messages.stream()
                .mapToInt(this::estimateTokens)
                .sum();
    }

    private int estimateTokens(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }
        // 粗略估算
        int chineseChars = 0;
        int otherChars = 0;
        for (char c : message.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                chineseChars++;
            } else {
                otherChars++;
            }
        }
        return (int) (chineseChars * 2 + otherChars * 0.4);
    }

    private String serializeMessage(Message message) {
        JSONObject json = new JSONObject();
        json.put("type", message.getMessageType().getValue());
        json.put("content", message.getContent());
        return json.toJSONString();
    }

    private Message deserializeMessage(String jsonStr) {
        JSONObject json = JSON.parseObject(jsonStr);
        String type = json.getString("type");
        String content = json.getString("content");

        if (MessageType.USER.getValue().equals(type)) {
            return new UserMessage(content);
        } else if (MessageType.ASSISTANT.getValue().equals(type)) {
            return new AssistantMessage(content);
        } else if (MessageType.SYSTEM.getValue().equals(type)) {
            return new SystemMessage(content);
        }
        // Fallback or other types
        return new UserMessage(content);
    }
}
