package com.classified.app.controller;

import com.classified.app.dto.request.CreateReportRequest;
import com.classified.app.model.Report;
import com.classified.app.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Create a report")
    public ResponseEntity<Report> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reportService.createReport(request, userDetails.getUsername()));
    }
}
