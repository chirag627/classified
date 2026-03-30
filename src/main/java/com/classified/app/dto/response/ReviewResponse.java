package com.classified.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private String id;
    private String reviewerId;
    private String reviewerName;
    private String sellerId;
    private String adId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
