package com.classified.app.controller;

import com.classified.app.dto.request.LoginRequest;
import com.classified.app.dto.request.RegisterRequest;
import com.classified.app.dto.response.AuthResponse;
import com.classified.app.dto.response.UserResponse;
import com.classified.app.exception.GlobalExceptionHandler;
import com.classified.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_validRequest_returns200() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        UserResponse response = UserResponse.builder()
                .id("user1")
                .name("Test User")
                .email("test@example.com")
                .roles(Set.of("USER"))
                .active(true)
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void register_missingEmail_returns400() throws Exception {
        String requestJson = "{\"name\":\"Test User\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validRequest_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .token("mock.jwt.token")
                .tokenType("Bearer")
                .id("user1")
                .name("Test User")
                .email("test@example.com")
                .roles(Set.of("USER"))
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
