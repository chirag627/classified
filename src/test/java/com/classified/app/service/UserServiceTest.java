package com.classified.app.service;

import com.classified.app.dto.request.LoginRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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
    private AuthenticationManager authenticationManager;

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
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setCity("Chicago");
        request.setState("IL");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pw");
        when(userRepository.save(any(User.class))).thenReturn(User.builder()
                .id("newUser1")
                .name("New User")
                .email("new@test.com")
                .password("encoded_pw")
                .roles(Set.of("USER"))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());

        UserResponse result = userService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getName()).isEqualTo("New User");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_existingEmail_throwsBadRequestException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setName("User");
        request.setPassword("password");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "user1", "encoded_password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken("user1", testUser.getRoles())).thenReturn("test_jwt_token");

        AuthResponse result = userService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("test_jwt_token");
        assertThat(result.getId()).isEqualTo("user1");
        assertThat(result.getEmail()).isEqualTo("test@test.com");
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
