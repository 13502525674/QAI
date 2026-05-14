package com.ape.apeadmin.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ape.apeadmin.domain.ApeChatMessage;
import com.ape.apeadmin.service.ApeChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, UserInfo> onlineUsers = new ConcurrentHashMap<>();
    
    // 为每个session添加锁对象，防止并发写入冲突
    private static final Map<String, Object> sessionLocks = new ConcurrentHashMap<>();

    private static ApeChatMessageService chatMessageService;

    @Autowired
    public void setChatMessageService(ApeChatMessageService chatMessageService) {
        ChatWebSocketHandler.chatMessageService = chatMessageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        sessions.put(userId, session);
        sessionLocks.put(userId, new Object()); // 为每个session创建锁对象
        onlineCount.incrementAndGet();
        log.info("用户连接: {}, 当前在线人数: {}", userId, onlineCount.get());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String userId = getUserIdFromSession(session);
        String payload = message.getPayload().toString();
        
        try {
            JSONObject jsonObject = JSON.parseObject(payload);
            String type = jsonObject.getString("type");
            
            switch (type) {
                case "init":
                    handleInit(userId, jsonObject);
                    break;
                case "private":
                    handlePrivateMessage(userId, jsonObject);
                    break;
                case "group":
                    handleGroupMessage(userId, jsonObject);
                    break;
                default:
                    log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("消息处理异常: ", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = getUserIdFromSession(session);
        log.error("用户 {} 的WebSocket错误: ", userId, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        sessions.remove(userId);
        sessionLocks.remove(userId); // 移除锁对象
        onlineUsers.remove(userId);
        onlineCount.decrementAndGet();
        
        log.info("用户断开连接: {}, 当前在线人数: {}", userId, onlineCount.get());
        
        sendOnlineUsersToAll();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String getUserIdFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void handleInit(String userId, JSONObject jsonObject) {
        String userName = jsonObject.getString("userName");
        
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUserName(userName);
        onlineUsers.put(userId, userInfo);
        
        sendOnlineUsersToAll();
    }

    private void handlePrivateMessage(String senderId, JSONObject jsonObject) {
        UserInfo senderInfo = onlineUsers.get(senderId);
        String senderName = senderInfo != null ? senderInfo.getUserName() : "用户";
        String receiverId = jsonObject.getString("receiverId");
        String content = jsonObject.getString("content");
        String contentType = jsonObject.getString("contentType");
        
        ApeChatMessage chatMessage = new ApeChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setSenderName(senderName);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setContent(content);
        chatMessage.setMessageType("private");
        chatMessage.setContentType(contentType != null ? contentType : "text");
        chatMessage.setCreateTime(new Date());
        
        if (chatMessageService != null) {
            chatMessageService.insert(chatMessage);
        }
        
        JSONObject result = new JSONObject();
        result.put("type", "private");
        result.put("id", chatMessage.getId());
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("receiverId", receiverId);
        result.put("content", content);
        result.put("contentType", chatMessage.getContentType());
        result.put("createTime", System.currentTimeMillis());
        
        if (sessions.containsKey(receiverId)) {
            sendMessage(sessions.get(receiverId), result.toJSONString());
        }
        
        if (sessions.containsKey(senderId)) {
            sendMessage(sessions.get(senderId), result.toJSONString());
        }
    }

    private void handleGroupMessage(String senderId, JSONObject jsonObject) {
        UserInfo senderInfo = onlineUsers.get(senderId);
        String senderName = senderInfo != null ? senderInfo.getUserName() : "用户";
        String content = jsonObject.getString("content");
        String contentType = jsonObject.getString("contentType");
        
        ApeChatMessage chatMessage = new ApeChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setSenderName(senderName);
        chatMessage.setReceiverId("GROUP");
        chatMessage.setContent(content);
        chatMessage.setMessageType("group");
        chatMessage.setContentType(contentType != null ? contentType : "text");
        chatMessage.setCreateTime(new Date());
        
        if (chatMessageService != null) {
            chatMessageService.insert(chatMessage);
        }
        
        JSONObject result = new JSONObject();
        result.put("type", "group");
        result.put("id", chatMessage.getId());
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("content", content);
        result.put("contentType", chatMessage.getContentType());
        result.put("createTime", System.currentTimeMillis());
        
        sendAll(result.toJSONString());
    }

    private void sendMessage(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        
        // 获取该session对应的锁对象
        String userId = getUserIdFromSession(session);
        Object lock = sessionLocks.get(userId);
        
        if (lock == null) {
            log.warn("未找到用户 {} 的锁对象", userId);
            return;
        }
        
        // 使用同步块确保线程安全
        synchronized (lock) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error("发送消息失败: ", e);
            }
        }
    }

    private void sendAll(String message) {
        // 遍历所有session并发送消息
        for (WebSocketSession session : sessions.values()) {
            try {
                sendMessage(session, message);
            } catch (Exception e) {
                log.error("广播消息失败: ", e);
            }
        }
    }

    private void sendOnlineUsersToAll() {
        JSONObject result = new JSONObject();
        result.put("type", "onlineUsers");
        result.put("users", onlineUsers.values());
        
        sendAll(result.toJSONString());
    }

    public static int getOnlineCount() {
        return onlineCount.get();
    }

    public static Map<String, UserInfo> getOnlineUsers() {
        return onlineUsers;
    }

    public static class UserInfo {
        private String userId;
        private String userName;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}
