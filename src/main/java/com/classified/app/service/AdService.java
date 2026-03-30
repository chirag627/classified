package com.classified.app.service;

import com.classified.app.dto.request.CreateAdRequest;
import com.classified.app.dto.request.UpdateAdRequest;
import com.classified.app.dto.response.AdResponse;
import com.classified.app.dto.response.PagedResponse;

import java.util.List;

public interface AdService {
    AdResponse createAd(CreateAdRequest request, String userId);
    AdResponse updateAd(String id, UpdateAdRequest request, String userId);
    void deleteAd(String id, String userId);
    AdResponse getAdById(String id);
    PagedResponse<AdResponse> getAllAds(int page, int size);
    PagedResponse<AdResponse> getAdsByCategory(String categoryId, int page, int size);
    PagedResponse<AdResponse> searchAds(String keyword, String categoryId, int page, int size);
    List<AdResponse> getFeaturedAds();
    List<AdResponse> getAdsByUser(String userId);
    AdResponse approveAd(String id);
    AdResponse rejectAd(String id);
    PagedResponse<AdResponse> getAdsByStatus(String status, int page, int size);
    void incrementViews(String id);
}
