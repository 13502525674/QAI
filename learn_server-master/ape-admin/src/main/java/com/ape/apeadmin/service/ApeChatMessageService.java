package com.ape.apeadmin.service;

import com.ape.apeadmin.domain.ApeChatMessage;

import java.util.List;

public interface ApeChatMessageService {

    void insert(ApeChatMessage message);

    List<ApeChatMessage> getGroupMessages(int limit, String userId);

    List<ApeChatMessage> getPrivateMessages(String userId1, String userId2);

    void deleteMessage(String messageId, String userId);

    void deleteAllGroupMessages(String userId);

    void deleteAllPrivateMessages(String userId1, String userId2);
}
