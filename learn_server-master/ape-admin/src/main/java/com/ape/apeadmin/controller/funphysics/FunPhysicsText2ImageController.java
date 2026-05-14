package com.ape.apeadmin.controller.funphysics;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apeadmin.service.ai.FunPhysicsImageService;
import com.ape.apesystem.domain.FunPhysicsImage;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.FunPhysicsImageDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@ResponseBody
@RequestMapping("/funphysics/text2image")
public class FunPhysicsText2ImageController {

    @Autowired
    private FunPhysicsImageService text2ImageService;

    @Autowired
    private FunPhysicsImageDbService imageDbService;

    @Log(name = "生成物理图片", type = BusinessType.OTHER)
    @PostMapping("generate")
    public Result generate(@RequestBody Map<String, String> params) {
        String prompt = params.get("prompt");
        String style = params.get("style");
        String conversationId = params.get("conversationId");
        
        if (prompt == null || prompt.trim().isEmpty()) {
            return Result.fail("提示词不能为空");
        }
        
        if (conversationId == null || conversationId.trim().isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        
        ApeUser user = ShiroUtils.getUserInfo();
        Map<String, Object> result = text2ImageService.generateAndSaveImage(prompt, style, user.getId(), conversationId);
        
        if (!(Boolean) result.get("success")) {
            return Result.fail((String) result.get("error"));
        }
        
        FunPhysicsImage image = new FunPhysicsImage();
        image.setId(UUID.randomUUID().toString());
        image.setUserId(user.getId());
        image.setPrompt(prompt);
        image.setEnhancedPrompt((String) result.get("enhancedPrompt"));
        image.setImageUrl((String) result.get("imageUrl"));
        image.setLocalPath((String) result.get("localUrl"));
        image.setStyle(style);
        image.setModel("wanx2.1-t2i-turbo");
        image.setLikesCount(0);
        image.setIsPublic(0);
        image.setStatus(0);
        image.setCreateTime(new Date());
        imageDbService.save(image);
        
        result.put("id", image.getId());
        result.put("conversationId", conversationId);
        return Result.success(result);
    }

    @Log(name = "生成图片提示词", type = BusinessType.OTHER)
    @PostMapping("generatePrompt")
    public Result generatePrompt(@RequestBody Map<String, String> params) {
        String idea = params.get("idea");
        String conversationId = params.get("conversationId");
        
        if (idea == null || idea.trim().isEmpty()) {
            return Result.fail("想法不能为空");
        }
        
        if (conversationId == null || conversationId.trim().isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        
        String prompt = text2ImageService.generatePromptFromIdea(idea, conversationId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("prompt", prompt);
        result.put("conversationId", conversationId);
        
        return Result.success(result);
    }

    @Log(name = "仅生成图片URL", type = BusinessType.OTHER)
    @PostMapping("generateUrl")
    public Result generateUrl(@RequestBody Map<String, String> params) {
        String prompt = params.get("prompt");
        String style = params.get("style");
        String conversationId = params.get("conversationId");
        
        if (prompt == null || prompt.trim().isEmpty()) {
            return Result.fail("提示词不能为空");
        }
        
        if (conversationId == null || conversationId.trim().isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        
        Map<String, Object> result = text2ImageService.generateImage(prompt, style, conversationId);
        
        if (!(Boolean) result.get("success")) {
            return Result.fail((String) result.get("error"));
        }
        
        result.put("conversationId", conversationId);
        return Result.success(result);
    }

    @Log(name = "获取物理提示词建议", type = BusinessType.OTHER)
    @GetMapping("getSuggestions")
    public Result getSuggestions() {
        List<String> suggestions = text2ImageService.getPhysicsPromptSuggestions();
        return Result.success(suggestions);
    }
}
