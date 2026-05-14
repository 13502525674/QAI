package com.ape.apeadmin.controller.ai;

import com.ape.apeadmin.service.ai.AiAssistantService;
import com.ape.apeadmin.service.ai.RAGService;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final RAGService ragService;
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "ai:chat:memory:";

    public AiAssistantController(AiAssistantService aiAssistantService, RAGService ragService, StringRedisTemplate redisTemplate) {
        this.aiAssistantService = aiAssistantService;
        this.ragService = ragService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/history")
    public List<Message> getHistory(@RequestParam(value = "conversationId", required = false) String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "default";
        }
        return aiAssistantService.getHistory(conversationId);
    }

    @GetMapping("/history/count")
    public Result getHistoryCount() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        long totalMessages = 0;
        int conversationCount = 0;
        
        if (keys != null && !keys.isEmpty()) {
            conversationCount = keys.size();
            for (String key : keys) {
                Long size = redisTemplate.opsForList().size(key);
                if (size != null) {
                    totalMessages += size;
                }
            }
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalMessages", totalMessages);
        result.put("conversationCount", conversationCount);
        return Result.success(result);
    }

    @PostMapping("/knowledge/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file) {
        ragService.ingestDocument(file);
        return "Document uploaded and ingested successfully.";
    }

    @GetMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chatStream(@RequestParam(value = "message", defaultValue = "你好") String message,
                                   @RequestParam(value = "conversationId", required = false) String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "default"; 
        }
        return aiAssistantService.chatStream(message, conversationId);
    }

    @Log(name = "AI助手调用", type = BusinessType.OTHER)
    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message", defaultValue = "你好") String message,
                       @RequestParam(value = "conversationId", required = false) String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "default";
        }
        return aiAssistantService.chat(message, conversationId);
    }

    @DeleteMapping("/history")
    public String clearHistory(@RequestParam(value = "conversationId", required = false) String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "default";
        }
        aiAssistantService.clearHistory(conversationId);
        return "History cleared";
    }
}
