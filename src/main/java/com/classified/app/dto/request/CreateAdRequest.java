package com.classified.app.dto.request;

import com.classified.app.model.Ad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateAdRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String categoryId;

    private String subcategoryId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    private boolean negotiable;
    private boolean hidePrice;

    private List<String> images = new ArrayList<>();

    private String city;
    private String state;
    private Double lat;
    private Double lng;

    private List<String> tags = new ArrayList<>();

    private Ad.Condition condition;
}
