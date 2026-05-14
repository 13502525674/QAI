package com.ape.apeadmin.controller.chat;

import com.ape.apeadmin.domain.ApeChatMessage;
import com.ape.apeadmin.service.ApeChatMessageService;
import com.ape.apeadmin.websocket.ChatWebSocketHandler;
import com.ape.apecommon.domain.Result;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeUser;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
@RequestMapping("/chatApi")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    
    @Value("${chat.upload.path:chat-uploads}")
    private String uploadPath;

    @Autowired
    private ApeChatMessageService chatMessageService;

    @GetMapping("/onlineUsers")
    public Result getOnlineUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        for (ChatWebSocketHandler.UserInfo userInfo : ChatWebSocketHandler.getOnlineUsers().values()) {
            Map<String, Object> user = new HashMap<>();
            user.put("userId", userInfo.getUserId());
            user.put("userName", userInfo.getUserName());
            users.add(user);
        }
        return Result.success(users);
    }

    @GetMapping("/groupMessages")
    public Result getGroupMessages(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        try {
            ApeUser user = ShiroUtils.getUserInfo();
            if (user == null) {
                return Result.fail("请先登录");
            }
            log.info("获取群聊历史消息, limit: {}", limit);
            List<ApeChatMessage> messages = chatMessageService.getGroupMessages(limit, user.getId());
            log.info("查询到群聊消息数量: {}", messages != null ? messages.size() : 0);
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取群聊消息失败", e);
            return Result.fail("获取群聊消息失败: " + e.getMessage());
        }
    }

    @GetMapping("/privateMessages")
    public Result getPrivateMessages(@RequestParam("userId2") String userId2) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String userId1 = user.getId();
        log.info("获取私聊历史消息, userId1: {}, userId2: {}", userId1, userId2);
        List<ApeChatMessage> messages = chatMessageService.getPrivateMessages(userId1, userId2);
        log.info("查询到私聊消息数量: {}", messages != null ? messages.size() : 0);
        return Result.success(messages);
    }

    @DeleteMapping("/message/{messageId}")
    public Result deleteMessage(@PathVariable("messageId") String messageId) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        chatMessageService.deleteMessage(messageId, user.getId());
        return Result.success("消息删除成功");
    }

    @DeleteMapping("/groupMessages")
    public Result deleteAllGroupMessages() {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        chatMessageService.deleteAllGroupMessages(user.getId());
        return Result.success("群聊记录已清空");
    }

    @DeleteMapping("/privateMessages/{userId2}")
    public Result deleteAllPrivateMessages(@PathVariable("userId2") String userId2) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String userId1 = user.getId();
        chatMessageService.deleteAllPrivateMessages(userId1, userId2);
        return Result.success("私聊记录已清空");
    }
    
    /**
     * 上传聊天图片
     */
    @PostMapping("/uploadImage")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        
        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }
        
        try {
            // 获取项目根目录的绝对路径
            String projectRoot = System.getProperty("user.dir");
            
            // 构建上传目录的绝对路径
            File uploadDir = new File(projectRoot, uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                log.info("创建上传目录: {}", uploadDir.getAbsolutePath());
            }
            
            // 生成文件名
            LocalDate today = LocalDate.now();
            Instant timestamp = Instant.now();
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            String filename = today + String.valueOf(timestamp.toEpochMilli()) + "." + ext;
            
            // 保存文件
            File destFile = new File(uploadDir, filename);
            file.transferTo(destFile);
            
            log.info("图片上传成功: {}, 路径: {}", filename, destFile.getAbsolutePath());
            
            // 返回图片访问URL
            Map<String, String> data = new HashMap<>();
            data.put("url", "/chatApi/images/" + filename);
            data.put("filename", filename);
            
            return Result.success(data);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.fail("图片上传失败: " + e.getMessage());
        }
    }
}
