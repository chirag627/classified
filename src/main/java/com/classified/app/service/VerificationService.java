package com.classified.app.service;

public interface VerificationService {
    void sendPhoneOtp(String userId, String countryCode, String mobileNumber);
    void verifyPhoneOtp(String userId, String countryCode, String mobileNumber, String otpCode);
    void sendEmailVerification(String userId);
    void verifyEmail(String token);
}
