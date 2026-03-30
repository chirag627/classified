package com.classified.app.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    private String city;
    private String state;
    private Double lat;
    private Double lng;
}
