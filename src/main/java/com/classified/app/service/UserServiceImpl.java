package com.classified.app.service;

import com.classified.app.client.AuthServiceClient;
import com.classified.app.dto.request.LoginRequest;
import com.classified.app.dto.request.RegisterRequest;
import com.classified.app.dto.response.AuthResponse;
import com.classified.app.dto.response.UserResponse;
import com.classified.app.exception.BadRequestException;
import com.classified.app.exception.ResourceNotFoundException;
import com.classified.app.model.PhoneOtpToken;
import com.classified.app.model.User;
import com.classified.app.repository.PhoneOtpRepository;
import com.classified.app.repository.UserRepository;
import com.classified.app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PhoneOtpRepository phoneOtpRepository;
    private final AuthServiceClient authServiceClient;

    @Override
    public UserResponse register(RegisterRequest request) {
        String phone = request.getPhone();
        if (phone == null || phone.isBlank()) {
            throw new BadRequestException("Phone number is required");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Phone number already registered: " + phone);
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }
        User.UserBuilder builder = User.builder()
                .name(request.getName())
                .phone(phone)
                .city(request.getCity())
                .state(request.getState())
                .roles(Set.of("USER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true);
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            builder.email(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            builder.password(passwordEncoder.encode(request.getPassword()));
        }
        return mapToResponse(userRepository.save(builder.build()));
    }

    // ─── Phone OTP Login ────────────────────────────────────────────────────

    @Override
    public void sendLoginOtp(String mobileNumber, String countryCode) {
        String cc = (countryCode != null && !countryCode.isBlank()) ? countryCode : "91";
        User user = userRepository.findByPhone(mobileNumber)
                .orElseThrow(() -> new BadRequestException(
                        "No account found with this phone number. Please register first."));

        String verificationId = authServiceClient.sendOtp(cc, mobileNumber);
        if (verificationId == null) {
            throw new BadRequestException("Failed to send OTP. Please try again.");
        }

        phoneOtpRepository.deleteByUserId(user.getId());
        phoneOtpRepository.save(PhoneOtpToken.builder()
                .userId(user.getId())
                .mobileNumber(mobileNumber)
                .countryCode(cc)
                .verificationId(verificationId)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .build());
        log.info("Login OTP sent to {} for user {}", mobileNumber, user.getId());
    }

    @Override
    public AuthResponse verifyLoginOtp(String mobileNumber, String countryCode, String otpCode) {
        String cc = (countryCode != null && !countryCode.isBlank()) ? countryCode : "91";
        User user = userRepository.findByPhone(mobileNumber)
                .orElseThrow(() -> new BadRequestException(
                        "No account found with this phone number."));

        PhoneOtpToken token = phoneOtpRepository.findByUserIdAndMobileNumber(user.getId(), mobileNumber)
                .orElseThrow(() -> new BadRequestException(
                        "No OTP found for this number. Please request a new one."));

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            phoneOtpRepository.delete(token);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        boolean verified = authServiceClient.verifyOtp(token.getCountryCode(), token.getVerificationId(), otpCode);
        if (!verified) {
            throw new BadRequestException("Invalid OTP. Please try again.");
        }

        phoneOtpRepository.delete(token);

        // Mark phone as verified on login
        if (!user.isPhoneVerified()) {
            user.setPhoneVerified(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        String jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getRoles());
        log.info("User {} logged in via phone OTP", user.getId());
        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    /** @deprecated kept for potential admin fallback */
    @Override
    public AuthResponse login(LoginRequest request) {
        throw new BadRequestException("Email/password login is disabled. Please use phone OTP login.");
    }

    // ─── Other Methods ───────────────────────────────────────────────────────

    @Override
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(String userId) {
        return getUserById(userId);
    }

    @Override
    public UserResponse updateUser(String id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setName(request.getName());
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Phone number already in use");
            }
            user.setPhone(request.getPhone());
            user.setPhoneVerified(false);
        }
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updatePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getPassword() == null) {
            throw new BadRequestException("No password set for this account.");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .city(user.getCity())
                .state(user.getState())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .active(user.isActive())
                .avgRating(user.getAvgRating())
                .phoneVerified(user.isPhoneVerified())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
