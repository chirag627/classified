package com.classified.app.dto.response;

import com.classified.app.model.Ad;
import com.classified.app.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdResponse {
    private String id;
    private String title;
    private String description;
    private String categoryId;
    private String categoryName;
    private String subcategoryId;
    private Double price;
    private boolean negotiable;
    private boolean hidePrice;
    private List<String> images;
    private Location location;
    private List<String> tags;
    private Ad.Condition condition;
    private String userId;
    private String userName;
    private String sellerPhone;
    private Ad.AdStatus status;
    private boolean featured;
    private boolean boosted;
    private Double boostAmount;
    private Integer boostDays;
    private LocalDateTime boostExpiry;
    private long views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private boolean favorited;
    private long favoriteCount;
}
