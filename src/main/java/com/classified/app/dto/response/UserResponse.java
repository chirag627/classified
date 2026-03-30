package com.classified.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private String city;
    private String state;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private boolean active;
    private double avgRating;
}
