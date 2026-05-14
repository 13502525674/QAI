package com.ape.apeadmin.controller.funphysics;

import com.ape.apeadmin.service.ai.FunPhysicsImageService;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.FunPhysicsImage;
import com.ape.apesystem.domain.FunPhysicsImageLike;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.FunPhysicsImageDbService;
import com.ape.apesystem.service.FunPhysicsImageLikeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@ResponseBody
@RequestMapping("/funphysics/image")
public class FunPhysicsImageController {

    @Autowired
    private FunPhysicsImageDbService imageService;

    @Autowired
    private FunPhysicsImageLikeService likeService;

    @Autowired
    private FunPhysicsImageService aiImageService;

    @Log(name = "获取公开画廊", type = BusinessType.OTHER)
    @GetMapping("getGallery")
    public Result getGallery(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Page<FunPhysicsImage> page = imageService.getPublicGallery(pageNum, pageSize);
        ApeUser user = ShiroUtils.getUserInfo();
        for (FunPhysicsImage img : page.getRecords()) {
            img.setIsLiked(likeService.hasLiked(img.getId(), user.getId()));
        }
        return Result.success(page);
    }

    @Log(name = "获取公开图片列表", type = BusinessType.OTHER)
    @GetMapping("getPublicImages")
    public Result getPublicImages(@RequestParam(value = "page", defaultValue = "1") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<FunPhysicsImage> imagePage = imageService.getPublicGallery(page, size);
        ApeUser user = ShiroUtils.getUserInfo();
        for (FunPhysicsImage img : imagePage.getRecords()) {
            img.setIsLiked(likeService.hasLiked(img.getId(), user.getId()));
        }
        return Result.success(imagePage.getRecords());
    }

    @Log(name = "获取热门图片", type = BusinessType.OTHER)
    @GetMapping("getHot")
    public Result getHot(@RequestParam(value = "count", defaultValue = "10") int count) {
        List<FunPhysicsImage> images = imageService.getHotImages(count);
        ApeUser user = ShiroUtils.getUserInfo();
        for (FunPhysicsImage img : images) {
            img.setIsLiked(likeService.hasLiked(img.getId(), user.getId()));
        }
        return Result.success(images);
    }

    @Log(name = "获取用户图片", type = BusinessType.OTHER)
    @GetMapping("getUserImages")
    public Result getUserImages() {
        ApeUser user = ShiroUtils.getUserInfo();
        List<FunPhysicsImage> images = imageService.getUserImages(user.getId());
        return Result.success(images);
    }

    @Log(name = "获取图片详情", type = BusinessType.OTHER)
    @GetMapping("getById")
    public Result getById(@RequestParam("id") String id) {
        ApeUser user = ShiroUtils.getUserInfo();
        FunPhysicsImage image = imageService.getImageWithUser(id, user.getId());
        return Result.success(image);
    }

    @Log(name = "保存生成的图片", type = BusinessType.INSERT)
    @PostMapping("save")
    public Result save(@RequestBody FunPhysicsImage image) {
        ApeUser user = ShiroUtils.getUserInfo();
        image.setId(UUID.randomUUID().toString());
        image.setUserId(user.getId());
        image.setCreateTime(new Date());
        image.setLikesCount(0);
        image.setIsPublic(1);
        image.setStatus(0);
        imageService.save(image);
        return Result.success(image);
    }

    @Log(name = "更新图片公开状态", type = BusinessType.UPDATE)
    @PostMapping("updatePublic")
    public Result updatePublic(@RequestBody Map<String, Object> params) {
        String imageId = (String) params.get("imageId");
        Integer isPublic = (Integer) params.get("isPublic");
        ApeUser user = ShiroUtils.getUserInfo();
        
        FunPhysicsImage image = imageService.getById(imageId);
        if (image != null && image.getUserId().equals(user.getId())) {
            image.setIsPublic(isPublic);
            image.setUpdateTime(new Date());
            imageService.updateById(image);
            return Result.success();
        }
        return Result.fail("无权限操作");
    }

    @Log(name = "点赞图片", type = BusinessType.OTHER)
    @PostMapping("like")
    public Result like(@RequestBody Map<String, String> params) {
        String imageId = params.get("imageId");
        ApeUser user = ShiroUtils.getUserInfo();
        
        if (likeService.hasLiked(imageId, user.getId())) {
            QueryWrapper<FunPhysicsImageLike> wrapper = new QueryWrapper<>();
            wrapper.eq("image_id", imageId).eq("user_id", user.getId());
            likeService.remove(wrapper);
            imageService.decrementLikes(imageId);
            return Result.success(false);
        } else {
            FunPhysicsImageLike like = new FunPhysicsImageLike();
            like.setId(UUID.randomUUID().toString());
            like.setImageId(imageId);
            like.setUserId(user.getId());
            like.setCreateTime(new Date());
            likeService.save(like);
            imageService.incrementLikes(imageId);
            return Result.success(true);
        }
    }

    @Log(name = "删除图片", type = BusinessType.DELETE)
    @GetMapping("remove")
    public Result remove(@RequestParam("id") String id) {
        ApeUser user = ShiroUtils.getUserInfo();
        FunPhysicsImage image = imageService.getById(id);
        if (image != null && image.getUserId().equals(user.getId())) {
            imageService.removeById(id);
            return Result.success();
        }
        return Result.fail("无权限操作");
    }

    @Log(name = "生成图片", type = BusinessType.OTHER)
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
        
        // 生成并保存图片
        Map<String, Object> result = aiImageService.generateAndSaveImage(prompt, style, user.getId(), conversationId);
        
        if (!(Boolean) result.get("success")) {
            return Result.fail((String) result.get("error"));
        }
        
        // 保存到数据库并记录积分
        FunPhysicsImage image = new FunPhysicsImage();
        image.setId(UUID.randomUUID().toString());
        image.setUserId(user.getId());
        image.setPrompt(prompt);
        image.setEnhancedPrompt((String) result.get("enhancedPrompt"));
        image.setImageUrl((String) result.get("imageUrl"));
        image.setLocalPath(result.get("localUrl") != null ? (String) result.get("localUrl") : null);
        image.setStyle(style);
        image.setModel("wanx2.1-t2i-turbo");
        image.setLikesCount(0);
        image.setIsPublic(1);
        image.setStatus(0);
        image.setCreateTime(new Date());
        imageService.save(image);
        
        // 返回结果
        result.put("id", image.getId());
        
        return Result.success(result);
    }

    @Log(name = "获取提示词建议", type = BusinessType.OTHER)
    @GetMapping("getSuggestions")
    public Result getSuggestions() {
        List<String> suggestions = aiImageService.getPhysicsPromptSuggestions();
        return Result.success(suggestions);
    }
}
