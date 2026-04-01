package com.classified.app.service;

import com.classified.app.dto.request.RegisterRequest;
import com.classified.app.dto.response.AuthResponse;
import com.classified.app.dto.response.UserResponse;
import com.classified.app.exception.BadRequestException;
import com.classified.app.model.User;
import com.classified.app.repository.UserRepository;
import com.classified.app.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private com.classified.app.repository.PhoneOtpRepository phoneOtpRepository;

    @Mock
    private com.classified.app.client.AuthServiceClient authServiceClient;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user1")
                .name("Test User")
                .email("test@test.com")
                .password("encoded_password")
                .phone("555-1234")
                .city("New York")
                .state("NY")
                .roles(new HashSet<>(Collections.singletonList("USER")))
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    @Test
    void register_newUser_returnsUserResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setName("New User");
        request.setPhone("9876543210");
        request.setCity("Chicago");
        request.setState("IL");

        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(User.builder()
                .id("newUser1")
                .name("New User")
                .phone("9876543210")
                .roles(Set.of("USER"))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());

        UserResponse result = userService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isEqualTo("9876543210");
        assertThat(result.getName()).isEqualTo("New User");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_existingPhone_throwsBadRequestException() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("9876543210");
        request.setName("User");

        when(userRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void sendLoginOtp_existingPhone_sendsOtp() {
        when(userRepository.findByPhone("9945601803")).thenReturn(Optional.of(testUser));
        when(authServiceClient.sendOtp("91", "9945601803")).thenReturn("ver123");

        userService.sendLoginOtp("9945601803", "91");

        verify(phoneOtpRepository).deleteByUserId("user1");
        verify(phoneOtpRepository).save(any());
    }

    @Test
    void sendLoginOtp_unknownPhone_throwsBadRequest() {
        when(userRepository.findByPhone("0000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.sendLoginOtp("0000000000", "91"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No account found");
    }

    @Test
    void getUserById_existingUser_returnsUserResponse() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getUserById("user1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user1");
        assertThat(result.getName()).isEqualTo("Test User");
    }

    @Test
    void getAllUsers_returnsListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@test.com");
    }
}
