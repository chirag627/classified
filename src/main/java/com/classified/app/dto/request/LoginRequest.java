package com.classified.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    private String countryCode = "91";
}
