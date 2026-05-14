package com.ape.apeadmin.config.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RedisChatMemory implements ChatMemory {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "ai:chat:memory:";

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
            // Optional: set expire
            // redisTemplate.expire(key, 1, TimeUnit.HOURS);
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
        
        return jsonMessages.stream()
                .map(this::deserializeMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(KEY_PREFIX + conversationId);
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
