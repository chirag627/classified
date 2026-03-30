package com.classified.app.service;

import com.classified.app.dto.request.CreateReviewRequest;
import com.classified.app.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest request, String reviewerId);
    List<ReviewResponse> getReviewsBySeller(String sellerId);
    List<ReviewResponse> getReviewsByReviewer(String reviewerId);
    List<ReviewResponse> getReviewsByAd(String adId);
    void deleteReview(String id, String userId);
}
