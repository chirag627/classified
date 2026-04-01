package com.classified.app.service;

import com.classified.app.dto.request.SendMessageRequest;
import com.classified.app.dto.response.MessageResponse;

import java.util.List;

public interface MessageService {
    MessageResponse sendMessage(SendMessageRequest request, String senderId);
    List<MessageResponse> getConversation(String userId1, String userId2);
    List<MessageResponse> getUserMessages(String userId);
    void markAsRead(String conversationId, String userId);
    void markAsReadConversation(String userId, String otherUserId);
    long getUnreadCount(String userId);
}
