package com.classified.app.service;

import com.classified.app.dto.response.AdResponse;

import java.util.List;

public interface FavoriteService {
    void addFavorite(String userId, String adId);
    void removeFavorite(String userId, String adId);
    List<AdResponse> getUserFavorites(String userId);
    boolean isFavorited(String userId, String adId);
    long getFavoriteCount(String adId);
}
