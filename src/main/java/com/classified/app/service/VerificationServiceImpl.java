package com.classified.app.service;

import com.classified.app.client.AuthServiceClient;
import com.classified.app.exception.BadRequestException;
import com.classified.app.exception.ResourceNotFoundException;
import com.classified.app.model.PhoneOtpToken;
import com.classified.app.model.User;
import com.classified.app.repository.PhoneOtpRepository;
import com.classified.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final UserRepository userRepository;
    private final PhoneOtpRepository phoneOtpRepository;
    private final AuthServiceClient authServiceClient;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    // ─── Phone OTP ──────────────────────────────────────────────────────────

    @Override
    public void sendPhoneOtp(String userId, String countryCode, String mobileNumber) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String verificationId = authServiceClient.sendOtp(countryCode, mobileNumber);
        if (verificationId == null) {
            throw new BadRequestException("Failed to send OTP. Please check the number and try again.");
        }

        phoneOtpRepository.deleteByUserId(userId);
        PhoneOtpToken token = PhoneOtpToken.builder()
                .userId(userId)
                .mobileNumber(mobileNumber)
                .countryCode(countryCode)
                .verificationId(verificationId)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .build();
        phoneOtpRepository.save(token);
    }

    @Override
    public void verifyPhoneOtp(String userId, String countryCode, String mobileNumber, String otpCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        PhoneOtpToken token = phoneOtpRepository.findByUserIdAndMobileNumber(userId, mobileNumber)
                .orElseThrow(() -> new BadRequestException("No OTP found for this number. Please request a new one."));

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            phoneOtpRepository.delete(token);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        boolean verified = authServiceClient.verifyOtp(
                token.getCountryCode(), token.getVerificationId(), otpCode);
        if (!verified) {
            throw new BadRequestException("Invalid OTP. Please try again.");
        }

        phoneOtpRepository.delete(token);
        user.setPhone(mobileNumber);
        user.setPhoneVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Phone verified for user {}", userId);
    }

    // ─── Email Verification ──────────────────────────────────────────────────

    @Override
    public void sendEmailVerification(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email address is already verified.");
        }

        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String verificationLink = baseUrl + "/api/verification/email/verify?token=" + token;
        emailService.sendEmailVerification(user.getEmail(), user.getName(), verificationLink);
        log.info("Verification email dispatched for user {}", userId);
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification link."));

        if (user.getEmailVerificationExpiry() == null
                || LocalDateTime.now().isAfter(user.getEmailVerificationExpiry())) {
            throw new BadRequestException("Verification link has expired. Please request a new one.");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Email verified for user {}", user.getId());
    }
}
