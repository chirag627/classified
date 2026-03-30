package com.classified.app.dto.request;

import com.classified.app.model.Ad;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAdRequest {
    private String title;
    private String description;
    private String categoryId;
    private String subcategoryId;
    private Double price;
    private Boolean negotiable;
    private List<String> images;
    private String city;
    private String state;
    private Double lat;
    private Double lng;
    private List<String> tags;
    private Ad.Condition condition;
}
