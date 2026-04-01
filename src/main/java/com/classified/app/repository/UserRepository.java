package com.classified.app.repository;

import com.classified.app.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    long countByActive(boolean active);
    Optional<User> findByEmailVerificationToken(String token);
}
