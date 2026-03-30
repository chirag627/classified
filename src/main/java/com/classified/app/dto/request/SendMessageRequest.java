package com.classified.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank(message = "Receiver ID is required")
    private String receiverId;

    @NotBlank(message = "Ad ID is required")
    private String adId;

    @NotBlank(message = "Content is required")
    private String content;
}
