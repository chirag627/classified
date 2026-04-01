package com.classified.app.controller;

import com.classified.app.dto.request.RegisterRequest;
import com.classified.app.dto.response.AuthResponse;
import com.classified.app.dto.response.UserResponse;
import com.classified.app.exception.BadRequestException;
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
    @Operation(summary = "Register a new user (phone required)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login/send-otp")
    @Operation(summary = "Send OTP to phone number for login")
    public ResponseEntity<Map<String, String>> sendLoginOtp(@RequestBody Map<String, String> body) {
        String mobileNumber = body.get("mobileNumber");
        String countryCode = body.getOrDefault("countryCode", "91");
        if (mobileNumber == null || mobileNumber.isBlank()) {
            throw new BadRequestException("Mobile number is required");
        }
        userService.sendLoginOtp(mobileNumber, countryCode);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/login/verify-otp")
    @Operation(summary = "Verify OTP and receive JWT token")
    public ResponseEntity<AuthResponse> verifyLoginOtp(@RequestBody Map<String, String> body) {
        String mobileNumber = body.get("mobileNumber");
        String countryCode = body.getOrDefault("countryCode", "91");
        String otpCode = body.get("otpCode");
        if (mobileNumber == null || mobileNumber.isBlank()) {
            throw new BadRequestException("Mobile number is required");
        }
        if (otpCode == null || otpCode.isBlank()) {
            throw new BadRequestException("OTP code is required");
        }
        return ResponseEntity.ok(userService.verifyLoginOtp(mobileNumber, countryCode, otpCode));
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
            throw new BadRequestException("Both old and new passwords are required");
        }
        if (newPassword.length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters long");
        }
        userService.updatePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }
}
