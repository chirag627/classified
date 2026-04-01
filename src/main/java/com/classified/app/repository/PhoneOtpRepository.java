package com.classified.app.repository;

import com.classified.app.model.PhoneOtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PhoneOtpRepository extends MongoRepository<PhoneOtpToken, String> {
    Optional<PhoneOtpToken> findByUserIdAndMobileNumber(String userId, String mobileNumber);
    void deleteByUserId(String userId);
}
