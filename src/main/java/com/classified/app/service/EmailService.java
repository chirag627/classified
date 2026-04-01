package com.classified.app.service;

public interface EmailService {
    void sendEmailVerification(String toEmail, String name, String verificationLink);
}
