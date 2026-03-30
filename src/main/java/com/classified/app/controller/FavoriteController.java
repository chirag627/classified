package com.classified.app.controller;

import com.classified.app.dto.response.AdResponse;
import com.classified.app.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Favorites management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{adId}")
    @Operation(summary = "Add ad to favorites")
    public ResponseEntity<Void> addFavorite(
            @PathVariable String adId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favoriteService.addFavorite(userDetails.getUsername(), adId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{adId}")
    @Operation(summary = "Remove ad from favorites")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable String adId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favoriteService.removeFavorite(userDetails.getUsername(), adId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get user favorites")
    public ResponseEntity<List<AdResponse>> getUserFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userDetails.getUsername()));
    }

    @GetMapping("/{adId}/status")
    @Operation(summary = "Check if ad is favorited")
    public ResponseEntity<Boolean> isFavorited(
            @PathVariable String adId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(favoriteService.isFavorited(userDetails.getUsername(), adId));
    }
}
