package com.ape.apeadmin.controller.funphysics;

import com.ape.apeadmin.service.ai.MomentsAiReplyService;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.FunPhysicsMoments;
import com.ape.apesystem.domain.FunPhysicsMomentsComment;
import com.ape.apesystem.domain.FunPhysicsMomentsLike;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.FunPhysicsMomentsService;
import com.ape.apesystem.service.FunPhysicsMomentsCommentService;
import com.ape.apesystem.service.FunPhysicsMomentsLikeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@ResponseBody
@RequestMapping("/funphysics/moments")
public class FunPhysicsMomentsController {

    @Autowired
    private FunPhysicsMomentsService momentsService;

    @Autowired
    private FunPhysicsMomentsCommentService commentService;

    @Autowired
    private FunPhysicsMomentsLikeService likeService;

    @Autowired
    private MomentsAiReplyService momentsAiReplyService;

    @Log(name = "获取朋友圈列表", type = BusinessType.OTHER)
    @PostMapping("getPage")
    public Result getPage(@RequestBody FunPhysicsMoments moments) {
        Page<FunPhysicsMoments> page = momentsService.getPage(moments);
        return Result.success(page);
    }

    @Log(name = "获取随机朋友圈", type = BusinessType.OTHER)
    @GetMapping("getRandom")
    public Result getRandom(@RequestParam(value = "count", defaultValue = "10") int count) {
        List<FunPhysicsMoments> momentsList = momentsService.getRandomMoments(count);
        ApeUser user = ShiroUtils.getUserInfo();
        for (FunPhysicsMoments m : momentsList) {
            m.setIsLiked(likeService.hasLiked(m.getId(), user.getId()));
        }
        return Result.success(momentsList);
    }

    @Log(name = "获取推荐朋友圈", type = BusinessType.OTHER)
    @GetMapping("getRecommend")
    public Result getRecommend() {
        List<FunPhysicsMoments> momentsList = momentsService.getRandomMoments(1);
        if (!momentsList.isEmpty()) {
            ApeUser user = ShiroUtils.getUserInfo();
            FunPhysicsMoments moments = momentsList.get(0);
            moments.setIsLiked(likeService.hasLiked(moments.getId(), user.getId()));
            return Result.success(moments);
        }
        return Result.success(null);
    }

    @Log(name = "获取朋友圈详情", type = BusinessType.OTHER)
    @GetMapping("getById")
    public Result getById(@RequestParam(value = "id") String id) {
        ApeUser user = ShiroUtils.getUserInfo();
        FunPhysicsMoments moments = momentsService.getMomentsWithComments(id, user.getId());
        List<FunPhysicsMomentsComment> comments = commentService.getCommentsByMomentsId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("moments", moments);
        result.put("comments", comments);
        return Result.success(result);
    }

    @Log(name = "点赞朋友圈", type = BusinessType.OTHER)
    @PostMapping("like")
    public Result like(@RequestBody Map<String, String> params) {
        String momentsId = params.get("momentsId");
        ApeUser user = ShiroUtils.getUserInfo();
        
        if (likeService.hasLiked(momentsId, user.getId())) {
            QueryWrapper<FunPhysicsMomentsLike> wrapper = new QueryWrapper<>();
            wrapper.eq("moments_id", momentsId).eq("user_id", user.getId());
            likeService.remove(wrapper);
            momentsService.decrementLikes(momentsId);
            return Result.success(false);
        } else {
            FunPhysicsMomentsLike like = new FunPhysicsMomentsLike();
            like.setId(UUID.randomUUID().toString());
            like.setMomentsId(momentsId);
            like.setUserId(user.getId());
            like.setCreateTime(new Date());
            likeService.save(like);
            momentsService.incrementLikes(momentsId);
            return Result.success(true);
        }
    }

    @Log(name = "评论朋友圈", type = BusinessType.OTHER)
    @PostMapping("comment")
    public Result comment(@RequestBody FunPhysicsMomentsComment comment) {
        ApeUser user = ShiroUtils.getUserInfo();
        comment.setId(UUID.randomUUID().toString());
        comment.setUserId(user.getId());
        comment.setCommenterName(user.getUserName());
        comment.setCommenterAvatar("/images/physicists/光头强.png");
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setIsAiReply(0);
        commentService.save(comment);

        FunPhysicsMoments moments = momentsService.getById(comment.getMomentsId());
        if (moments != null) {
            FunPhysicsMomentsComment aiReply = momentsAiReplyService.generateAiReply(
                moments, 
                comment.getContent(), 
                user.getUserName()
            );
            if (aiReply != null) {
                commentService.save(aiReply);
            }
        }

        return Result.success(comment);
    }

    @Log(name = "获取评论列表", type = BusinessType.OTHER)
    @GetMapping("getComments")
    public Result getComments(@RequestParam(value = "momentsId") String momentsId) {
        List<FunPhysicsMomentsComment> comments = commentService.getCommentsByMomentsId(momentsId);
        return Result.success(comments);
    }

    @Log(name = "新增朋友圈", type = BusinessType.INSERT)
    @PostMapping("save")
    public Result save(@RequestBody FunPhysicsMoments moments) {
        moments.setId(UUID.randomUUID().toString());
        moments.setCreateTime(new Date());
        moments.setLikesCount(0);
        moments.setStatus(0);
        momentsService.save(moments);
        return Result.success();
    }

    @Log(name = "编辑朋友圈", type = BusinessType.UPDATE)
    @PostMapping("update")
    public Result update(@RequestBody FunPhysicsMoments moments) {
        moments.setUpdateTime(new Date());
        momentsService.updateById(moments);
        return Result.success();
    }

    @Log(name = "删除朋友圈", type = BusinessType.DELETE)
    @GetMapping("remove")
    public Result remove(@RequestParam String id) {
        momentsService.removeById(id);
        return Result.success();
    }
}
