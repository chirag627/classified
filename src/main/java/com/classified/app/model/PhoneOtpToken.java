package com.classified.app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "phone_otps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneOtpToken {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String mobileNumber;
    private String countryCode;

    /** verificationId returned by MessageCentral after sending OTP */
    private String verificationId;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
