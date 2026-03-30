package com.classified.app.controller;

import com.classified.app.dto.request.CreateReviewRequest;
import com.classified.app.dto.response.ReviewResponse;
import com.classified.app.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a review", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reviewService.createReview(request, userDetails.getUsername()));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get reviews for a seller")
    public ResponseEntity<List<ReviewResponse>> getReviewsBySeller(@PathVariable String sellerId) {
        return ResponseEntity.ok(reviewService.getReviewsBySeller(sellerId));
    }

    @GetMapping("/ad/{adId}")
    @Operation(summary = "Get reviews for an ad")
    public ResponseEntity<List<ReviewResponse>> getReviewsByAd(@PathVariable String adId) {
        return ResponseEntity.ok(reviewService.getReviewsByAd(adId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Void> deleteReview(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.deleteReview(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
