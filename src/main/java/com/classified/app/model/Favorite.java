package com.classified.app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "favorites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "user_ad_idx", def = "{'userId': 1, 'adId': 1}", unique = true)
public class Favorite {
    @Id
    private String id;

    private String userId;
    private String adId;
    private LocalDateTime createdAt;
}
