package com.classified.app.service;

import com.classified.app.dto.request.SendMessageRequest;
import com.classified.app.dto.response.MessageResponse;
import com.classified.app.model.Message;
import com.classified.app.model.User;
import com.classified.app.repository.MessageRepository;
import com.classified.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    public MessageResponse sendMessage(SendMessageRequest request, String senderId) {
        String conversationId = generateConversationId(senderId, request.getReceiverId(), request.getAdId());
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .adId(request.getAdId())
                .content(request.getContent())
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        return mapToResponse(messageRepository.save(message));
    }

    @Override
    public List<MessageResponse> getConversation(String userId1, String userId2) {
        return messageRepository.findConversationBetweenUsers(userId1, userId2).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageResponse> getUserMessages(String userId) {
        return messageRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(String conversationId, String userId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        messages.stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
    }

    @Override
    public void markAsReadConversation(String userId, String otherUserId) {
        List<Message> messages = messageRepository.findConversationBetweenUsers(userId, otherUserId);
        messages.stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
    }

    @Override
    public long getUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndReadFalse(userId);
    }

    private String generateConversationId(String userId1, String userId2, String adId) {
        String sorted = userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
        return sorted + "_" + adId;
    }

    private MessageResponse mapToResponse(Message message) {
        String senderName = userRepository.findById(message.getSenderId())
                .map(User::getName).orElse("Unknown");
        String receiverName = userRepository.findById(message.getReceiverId())
                .map(User::getName).orElse("Unknown");
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .senderName(senderName)
                .receiverId(message.getReceiverId())
                .receiverName(receiverName)
                .adId(message.getAdId())
                .content(message.getContent())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
