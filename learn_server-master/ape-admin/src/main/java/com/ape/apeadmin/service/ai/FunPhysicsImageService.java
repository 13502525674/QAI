package com.ape.apeadmin.service.ai;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import com.ape.apecommon.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@Service
public class FunPhysicsImageService {

    private static final Logger log = LoggerFactory.getLogger(FunPhysicsImageService.class);

    private static final String IMAGE_MODEL = "wanx2.1-t2i-turbo";
    // qwen-max is the default for chat if not specified, but good to be explicit if needed. 
    // However, spring-ai-alibaba usually defaults to qwen-turbo or plus. 
    // The user asked to call "qwen3-max". 
    // Assuming the ChatClient is configured to use the model defined in application.yml (qwen3-max).

    @Autowired
    private ImageModel imageModel;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Value("${funphysics.image.save-path:img/funphysics}")
    private String imageSavePath;

    @Value("classpath:prompts/system_physics_image_tutor.txt")
    private Resource systemPrompt;

    public Map<String, Object> generateImage(String prompt, String style, String conversationId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. Enhance prompt using ChatClient with Memory and System Prompt
            String enhancedPrompt = enhancePromptWithAI(prompt, style, conversationId);
            
            // 2. Generate Image using wanx2.1-t2i-turbo
            DashScopeImageOptions.Builder optionsBuilder = DashScopeImageOptions.builder()
                    .withModel(IMAGE_MODEL)
                    .withN(1);
            
            String imageUrl = imageModel.call(
                    new ImagePrompt(enhancedPrompt, optionsBuilder.build())
            ).getResult().getOutput().getUrl();
            
            result.put("success", true);
            result.put("imageUrl", imageUrl);
            result.put("enhancedPrompt", enhancedPrompt);
            result.put("originalPrompt", prompt);
            
            log.info("Generated image for prompt: {}", enhancedPrompt);
            
        } catch (Exception e) {
            log.error("Failed to generate image: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> generateAndSaveImage(String prompt, String style, String userId, String conversationId) {
        Map<String, Object> generateResult = generateImage(prompt, style, conversationId);
        
        if (!(Boolean) generateResult.get("success")) {
            return generateResult;
        }
        
        String imageUrl = (String) generateResult.get("imageUrl");
        try {
            Map<String, String> saveResult = downloadAndSaveImage(imageUrl, userId);
            generateResult.put("localPath", saveResult.get("localPath"));
            generateResult.put("localUrl", saveResult.get("localUrl"));
        } catch (Exception e) {
            log.error("Failed to save image: {}", e.getMessage(), e);
            generateResult.put("saveError", e.getMessage());
        }
        
        return generateResult;
    }

    public String generatePromptFromIdea(String idea, String conversationId) {
        try {
            String userMessage = String.format(
                "用户想要生成一张物理主题的图片，用户的想法是：%s\n" +
                "请根据这个想法，生成一个详细的、适合文生图模型的中文提示词。\n" +
                "提示词应该包含：\n" +
                "1. 详细的场景描述\n" +
                "2. 物理元素的细节\n" +
                "3. 艺术风格建议\n" +
                "4. 色彩和光影效果\n\n" +
                "请只返回提示词内容，不要包含其他解释或说明。",
                idea
            );

            // Using ChatClient with System Prompt and Memory to generate prompt
            String prompt = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .advisors(new MessageChatMemoryAdvisor(chatMemory))
                    .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                            .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                    .call()
                    .content();

            log.info("Generated prompt from idea: {} -> {}", idea, prompt);
            return prompt;
        } catch (Exception e) {
            log.error("Failed to generate prompt from idea: {}", e.getMessage(), e);
            return idea;
        }
    }

    private String enhancePromptWithAI(String prompt, String style, String conversationId) {
        try {
             String styleDesc = "";
             if (style != null) {
                switch (style) {
                    case "cartoon": styleDesc = "卡通风格，可爱活泼"; break;
                    case "realistic": styleDesc = "写实风格，真实细腻"; break;
                    case "artistic": styleDesc = "艺术风格，抽象创意"; break;
                    case "minimalist": styleDesc = "极简风格，简洁优雅"; break;
                    default: styleDesc = "科普插画风格";
                }
             }

             String userMessage = String.format(
                "用户输入提示词：%s\n" +
                "用户选择风格：%s\n" +
                "请结合用户输入和风格，生成一个最终的、优化的文生图提示词。\n" +
                "确保包含物理细节和视觉效果描述。只返回提示词内容。",
                prompt, styleDesc
             );

             return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .advisors(new MessageChatMemoryAdvisor(chatMemory))
                    .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                            .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                    .call()
                    .content();

        } catch (Exception e) {
            log.warn("Failed to enhance prompt with AI, falling back to manual enhancement: {}", e.getMessage());
            return manualEnhancePrompt(prompt, style);
        }
    }

    private String manualEnhancePrompt(String prompt, String style) {
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("物理主题，科学插画风格，");
        if (style != null) {
            switch (style) {
                case "cartoon": enhanced.append("卡通风格，可爱活泼，"); break;
                case "realistic": enhanced.append("写实风格，真实细腻，"); break;
                case "artistic": enhanced.append("艺术风格，抽象创意，"); break;
                case "minimalist": enhanced.append("极简风格，简洁优雅，"); break;
                default: enhanced.append("科普插画风格，");
            }
        }
        enhanced.append(prompt);
        enhanced.append("，高清，细节丰富，适合教学展示");
        return enhanced.toString();
    }

    private Map<String, String> downloadAndSaveImage(String imageUrl, String userId) throws IOException {
        String projectPath = System.getProperty("user.dir");
        String fullPath = projectPath + File.separator + imageSavePath.replace("/", File.separator);
        
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String fileName = UUID.randomUUID().toString() + ".png";
        String localPath = fullPath + File.separator + fileName;
        
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(localPath)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
        String localUrl = "/" + imageSavePath + "/" + fileName;
        
        Map<String, String> result = new HashMap<>();
        result.put("localPath", localPath);
        result.put("localUrl", localUrl);
        result.put("fileName", fileName);
        
        return result;
    }

    public List<String> getPhysicsPromptSuggestions() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("牛顿发现万有引力定律，苹果从树上掉落");
        suggestions.add("爱因斯坦思考相对论，时空弯曲");
        suggestions.add("电磁感应现象，法拉第的实验");
        suggestions.add("光的折射与反射，彩虹的形成");
        suggestions.add("原子结构模型，电子绕核运动");
        suggestions.add("量子力学双缝干涉实验");
        suggestions.add("热力学第二定律，熵增原理");
        suggestions.add("声波的传播与共振现象");
        suggestions.add("磁悬浮列车原理");
        suggestions.add("黑洞吞噬恒星");
        suggestions.add("薛定谔的猫思想实验");
        suggestions.add("粒子加速器对撞实验");
        return suggestions;
    }
}
