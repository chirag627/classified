package com.classified.app.repository;

import com.classified.app.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findBySellerId(String sellerId);
    List<Review> findByReviewerId(String reviewerId);
    List<Review> findByAdId(String adId);
    boolean existsByReviewerIdAndAdId(String reviewerId, String adId);
    double findAvgRatingBySellerId(String sellerId);
}
