package com.classified.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String id;
    private String name;
    private String email;
    private Set<String> roles;
}
