package com.classified.app.controller;

import com.classified.app.dto.request.CreateAdRequest;
import com.classified.app.dto.request.UpdateAdRequest;
import com.classified.app.dto.response.AdResponse;
import com.classified.app.dto.response.PagedResponse;
import com.classified.app.service.AdService;
import com.classified.app.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Tag(name = "Ads", description = "Ad management endpoints")
public class AdController {

    private final AdService adService;
    private final FileStorageService fileStorageService;

    @GetMapping
    @Operation(summary = "Get all active ads")
    public ResponseEntity<PagedResponse<AdResponse>> getAllAds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(adService.getAllAds(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Search ads")
    public ResponseEntity<PagedResponse<AdResponse>> searchAds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword : "";
        return ResponseEntity.ok(adService.searchAds(kw, categoryId, page, size));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured ads")
    public ResponseEntity<List<AdResponse>> getFeaturedAds() {
        return ResponseEntity.ok(adService.getFeaturedAds());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get ads by category")
    public ResponseEntity<PagedResponse<AdResponse>> getAdsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(adService.getAdsByCategory(categoryId, page, size));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get ads by user")
    public ResponseEntity<List<AdResponse>> getAdsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(adService.getAdsByUser(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ad by ID")
    public ResponseEntity<AdResponse> getAdById(@PathVariable String id) {
        adService.incrementViews(id);
        return ResponseEntity.ok(adService.getAdById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new ad", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<AdResponse> createAd(
            @Valid @RequestBody CreateAdRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(adService.createAd(request, userDetails.getUsername()));
    }

    @PostMapping("/upload-images")
    @Operation(summary = "Upload ad images", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<List<String>> uploadImages(
            @RequestParam("files") List<MultipartFile> files) {
        List<String> paths = fileStorageService.storeFiles(files);
        return ResponseEntity.ok(paths);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an ad", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<AdResponse> updateAd(
            @PathVariable String id,
            @RequestBody UpdateAdRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(adService.updateAd(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an ad", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Void> deleteAd(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        adService.deleteAd(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
