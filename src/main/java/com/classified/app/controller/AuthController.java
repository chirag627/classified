package com.classified.app.controller;

import com.classified.app.dto.request.LoginRequest;
import com.classified.app.dto.request.RegisterRequest;
import com.classified.app.dto.response.AuthResponse;
import com.classified.app.dto.response.UserResponse;
import com.classified.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth endpoints")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PutMapping("/profile/{id}")
    @Operation(summary = "Update user profile", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String id,
            @RequestBody RegisterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PutMapping("/password/{id}")
    @Operation(summary = "Change user password", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Void> changePassword(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new com.classified.app.exception.BadRequestException("Both old and new passwords are required");
        }
        if (newPassword.length() < 6) {
            throw new com.classified.app.exception.BadRequestException("New password must be at least 6 characters long");
        }
        userService.updatePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }
}
