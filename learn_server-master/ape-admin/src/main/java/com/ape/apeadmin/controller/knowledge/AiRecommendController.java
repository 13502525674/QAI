package com.ape.apeadmin.controller.knowledge;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeadmin.service.ai.AiAssistantService;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiRecommendController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @Autowired
    private ApeUserService apeUserService;

    @Log(name = "获取个性化推荐", type = BusinessType.OTHER)
    @GetMapping(value = "/recommend/{studentId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getPersonalizedRecommend(@PathVariable String studentId,
                                                  @RequestParam(defaultValue = "default") String conversationId) {
        return aiAssistantService.personalizedRecommend(studentId, conversationId);
    }

    @Log(name = "获取当前学生个性化推荐", type = BusinessType.OTHER)
    @GetMapping(value = "/recommend/my", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getMyPersonalizedRecommend(
            @RequestParam(value = "conversationId", defaultValue = "default") String conversationId,
            @RequestParam(value = "x_access_token", required = false) String tokenParam,
            HttpServletRequest request) {
        String token = request.getHeader(Constants.X_ACCESS_TOKEN);
        if (token == null || token.isEmpty()) {
            token = request.getParameter(Constants.X_ACCESS_TOKEN);
        }
        if (token == null || token.isEmpty()) {
            token = tokenParam;
        }
        
        ApeUser user = null;
        if (token != null && !token.isEmpty()) {
            try {
                user = apeUserService.getById(com.ape.apecommon.utils.JwtUtil.getUserId(token));
            } catch (Exception e) {
                return Flux.just("错误：无效的登录凭证");
            }
        }
        
        if (user == null) {
            return Flux.just("错误：用户未登录");
        }
        return aiAssistantService.personalizedRecommend(user.getId(), conversationId);
    }

    @Log(name = "获取学生画像数据", type = BusinessType.OTHER)
    @GetMapping("/profile/{studentId}")
    public Result getStudentProfile(@PathVariable String studentId) {
        Map<String, Object> profile = aiAssistantService.getStudentProfileForAI(studentId);
        return Result.success(profile);
    }

    @Log(name = "构建带学生画像的Prompt", type = BusinessType.OTHER)
    @PostMapping("/buildPrompt")
    public Result buildPromptWithProfile(@RequestParam String studentId, 
                                          @RequestParam String userMessage) {
        String prompt = aiAssistantService.buildPromptWithStudentProfile(studentId, userMessage);
        return Result.success(prompt);
    }
}
