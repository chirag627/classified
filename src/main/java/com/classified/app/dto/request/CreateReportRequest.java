package com.classified.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReportRequest {
    @NotBlank(message = "Ad ID is required")
    private String adId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String description;
}
