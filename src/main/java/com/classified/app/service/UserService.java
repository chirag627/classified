package com.classified.app.service;

import com.classified.app.dto.request.LoginRequest;
import com.classified.app.dto.request.RegisterRequest;
import com.classified.app.dto.response.AuthResponse;
import com.classified.app.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getUserById(String id);
    UserResponse updateUser(String id, RegisterRequest request);
    void deleteUser(String id);
    List<UserResponse> getAllUsers();
    UserResponse getCurrentUser(String userId);
    void updatePassword(String userId, String oldPassword, String newPassword);
}
