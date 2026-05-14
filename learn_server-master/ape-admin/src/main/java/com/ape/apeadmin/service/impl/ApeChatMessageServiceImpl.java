package com.ape.apeadmin.service.impl;

import com.ape.apeadmin.domain.ApeChatMessage;
import com.ape.apeadmin.mapper.ApeChatMessageMapper;
import com.ape.apeadmin.service.ApeChatMessageService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApeChatMessageServiceImpl implements ApeChatMessageService {

    @Autowired
    private ApeChatMessageMapper chatMessageMapper;

    @Override
    public void insert(ApeChatMessage message) {
        if (message.getId() == null || message.getId().isEmpty()) {
            message.setId(IdWorker.get32UUID());
        }
        chatMessageMapper.insert(message);
    }

    @Override
    public List<ApeChatMessage> getGroupMessages(int limit, String userId) {
        return chatMessageMapper.selectGroupMessages(limit, userId);
    }

    @Override
    public List<ApeChatMessage> getPrivateMessages(String userId1, String userId2) {
        return chatMessageMapper.selectPrivateMessages(userId1, userId2);
    }

    @Override
    public void deleteMessage(String messageId, String userId) {
        ApeChatMessage message = chatMessageMapper.selectById(messageId);
        if (message != null) {
            String deletedBy = message.getDeletedBy();
            if (deletedBy == null || deletedBy.isEmpty()) {
                message.setDeletedBy(userId);
            } else if (!deletedBy.contains(userId)) {
                message.setDeletedBy(deletedBy + "," + userId);
            }
            chatMessageMapper.updateById(message);
        }
    }

    @Override
    public void deleteAllGroupMessages(String userId) {
        List<ApeChatMessage> messages = chatMessageMapper.selectAllVisibleGroupMessages(userId);
        for (ApeChatMessage message : messages) {
            String deletedBy = message.getDeletedBy();
            if (deletedBy == null || deletedBy.isEmpty()) {
                message.setDeletedBy(userId);
            } else if (!deletedBy.contains(userId)) {
                message.setDeletedBy(deletedBy + "," + userId);
            }
            chatMessageMapper.updateById(message);
        }
    }

    @Override
    public void deleteAllPrivateMessages(String userId1, String userId2) {
        List<ApeChatMessage> messages = chatMessageMapper.selectAllVisiblePrivateMessages(userId1, userId2);
        for (ApeChatMessage message : messages) {
            String deletedBy = message.getDeletedBy();
            if (deletedBy == null || deletedBy.isEmpty()) {
                message.setDeletedBy(userId1);
            } else if (!deletedBy.contains(userId1)) {
                message.setDeletedBy(deletedBy + "," + userId1);
            }
            chatMessageMapper.updateById(message);
        }
    }
}
