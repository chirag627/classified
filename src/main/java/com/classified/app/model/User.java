package com.classified.app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String phone;
    private String password;
    private String profileImage;
    private String city;
    private String state;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private double avgRating = 0.0;
}
