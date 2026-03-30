package com.classified.app.controller;

import com.classified.app.dto.response.AdResponse;
import com.classified.app.dto.response.PagedResponse;
import com.classified.app.dto.response.UserResponse;
import com.classified.app.model.Report;
import com.classified.app.repository.AdRepository;
import com.classified.app.repository.UserRepository;
import com.classified.app.service.AdService;
import com.classified.app.service.ReportService;
import com.classified.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;
    private final AdService adService;
    private final ReportService reportService;
    private final AdRepository adRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeAds = adRepository.countByStatus(com.classified.app.model.Ad.AdStatus.ACTIVE);
        long pendingAds = adRepository.countByStatus(com.classified.app.model.Ad.AdStatus.PENDING);
        long pendingReports = reportService.getPendingReportsCount();
        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "activeAds", activeAds,
                "pendingAds", pendingAds,
                "pendingReports", pendingReports
        ));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ads")
    @Operation(summary = "Get ads by status")
    public ResponseEntity<PagedResponse<AdResponse>> getAdsByStatus(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adService.getAdsByStatus(status, page, size));
    }

    @PutMapping("/ads/{id}/approve")
    @Operation(summary = "Approve an ad")
    public ResponseEntity<AdResponse> approveAd(@PathVariable String id) {
        return ResponseEntity.ok(adService.approveAd(id));
    }

    @PutMapping("/ads/{id}/reject")
    @Operation(summary = "Reject an ad")
    public ResponseEntity<AdResponse> rejectAd(@PathVariable String id) {
        return ResponseEntity.ok(adService.rejectAd(id));
    }

    @GetMapping("/reports")
    @Operation(summary = "Get reports by status")
    public ResponseEntity<PagedResponse<Report>> getReports(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status, page, size));
    }

    @PutMapping("/reports/{id}/status")
    @Operation(summary = "Update report status")
    public ResponseEntity<Report> updateReportStatus(
            @PathVariable String id,
            @RequestParam String status) {
        return ResponseEntity.ok(reportService.updateReportStatus(id, Report.ReportStatus.valueOf(status.toUpperCase())));
    }
}
