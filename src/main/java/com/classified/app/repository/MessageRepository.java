package com.classified.app.repository;

import com.classified.app.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    List<Message> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(String senderId, String receiverId);
    long countByReceiverIdAndReadFalse(String receiverId);

    @Query(value = "{ $or: [ { 'senderId': ?0, 'receiverId': ?1 }, { 'senderId': ?1, 'receiverId': ?0 } ] }", sort = "{ 'createdAt': -1 }")
    List<Message> findConversationBetweenUsers(String userId1, String userId2);
}
