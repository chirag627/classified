package com.classified.app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    private String id;

    @Indexed
    private String conversationId;

    private String senderId;
    private String receiverId;
    private String adId;
    private String content;

    @Builder.Default
    private boolean read = false;

    private LocalDateTime createdAt;
}
