package com.classified.app.controller;

import com.classified.app.exception.BadRequestException;
import com.classified.app.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Tag(name = "Verification", description = "Phone OTP and email verification endpoints")
public class VerificationController {

    private final VerificationService verificationService;

    // ─── Phone OTP ──────────────────────────────────────────────────────────

    @PostMapping("/phone/send-otp")
    @Operation(summary = "Send OTP to phone number via auth service",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Map<String, String>> sendPhoneOtp(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String countryCode = body.getOrDefault("countryCode", "91");
        String mobileNumber = body.get("mobileNumber");
        if (mobileNumber == null || mobileNumber.isBlank()) {
            throw new BadRequestException("Mobile number is required");
        }
        verificationService.sendPhoneOtp(userDetails.getUsername(), countryCode, mobileNumber);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/phone/verify-otp")
    @Operation(summary = "Verify OTP and mark phone as verified",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Map<String, String>> verifyPhoneOtp(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String countryCode = body.getOrDefault("countryCode", "91");
        String mobileNumber = body.get("mobileNumber");
        String otpCode = body.get("otpCode");
        if (mobileNumber == null || mobileNumber.isBlank()) {
            throw new BadRequestException("Mobile number is required");
        }
        if (otpCode == null || otpCode.isBlank()) {
            throw new BadRequestException("OTP code is required");
        }
        verificationService.verifyPhoneOtp(userDetails.getUsername(), countryCode, mobileNumber, otpCode);
        return ResponseEntity.ok(Map.of("message", "Phone verified successfully"));
    }

    // ─── Email Verification ──────────────────────────────────────────────────

    @PostMapping("/email/send")
    @Operation(summary = "Send email verification link",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Map<String, String>> sendEmailVerification(
            @AuthenticationPrincipal UserDetails userDetails) {

        verificationService.sendEmailVerification(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Verification email sent. Please check your inbox."));
    }

    @GetMapping("/email/verify")
    @Operation(summary = "Verify email via token link (public — accessed from email)")
    public void verifyEmail(@RequestParam String token,
                            HttpServletResponse response) throws IOException {
        try {
            verificationService.verifyEmail(token);
            response.sendRedirect("/user/profile?emailVerified=true");
        } catch (Exception e) {
            response.sendRedirect("/user/profile?emailError=true");
        }
    }
}
