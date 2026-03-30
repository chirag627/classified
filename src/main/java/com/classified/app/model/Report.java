package com.classified.app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    private String id;

    private String reporterId;
    private String adId;
    private String reason;
    private String description;

    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime createdAt;

    public enum ReportStatus {
        PENDING, REVIEWED, DISMISSED
    }
}
