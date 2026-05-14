package com.ape.apeadmin.service.ai;

import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface AiAssistantService {
    Flux<String> chatStream(String message, String conversationId);
    String chat(String message, String conversationId);
    List<Message> getHistory(String conversationId);
    void clearHistory(String conversationId);
    
    String buildPromptWithStudentProfile(String studentId, String userMessage);
    Flux<String> personalizedRecommend(String studentId, String conversationId);
    Map<String, Object> getStudentProfileForAI(String studentId);
}
