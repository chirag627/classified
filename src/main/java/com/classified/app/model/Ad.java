package com.classified.app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ad {
    @Id
    private String id;

    private String title;
    private String description;

    @Indexed
    private String categoryId;

    private String subcategoryId;
    private Double price;
    private boolean negotiable;
    
    @Builder.Default
    private boolean hidePrice = false;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private Location location;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private Condition condition;

    @Indexed
    private String userId;

    @Builder.Default
    private AdStatus status = AdStatus.PENDING;

    @Builder.Default
    private boolean featured = false;

    private Double boostAmount;
    private Integer boostDays;
    private LocalDateTime boostExpiry;

    @Builder.Default
    private long views = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    public enum Condition {
        NEW, USED
    }

    public enum AdStatus {
        ACTIVE, PENDING, REJECTED, EXPIRED
    }
}
