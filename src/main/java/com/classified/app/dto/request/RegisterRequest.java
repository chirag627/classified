package com.classified.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String countryCode = "91";

    @Email(message = "Invalid email format")
    private String email;

    private String password;
    private String city;
    private String state;
}
