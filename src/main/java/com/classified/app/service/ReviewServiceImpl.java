package com.classified.app.service;

import com.classified.app.dto.request.CreateReviewRequest;
import com.classified.app.dto.response.ReviewResponse;
import com.classified.app.exception.BadRequestException;
import com.classified.app.exception.ResourceNotFoundException;
import com.classified.app.exception.UnauthorizedException;
import com.classified.app.model.Review;
import com.classified.app.model.User;
import com.classified.app.repository.ReviewRepository;
import com.classified.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponse createReview(CreateReviewRequest request, String reviewerId) {
        if (reviewRepository.existsByReviewerIdAndAdId(reviewerId, request.getAdId())) {
            throw new BadRequestException("You have already reviewed this ad");
        }
        Review review = Review.builder()
                .reviewerId(reviewerId)
                .sellerId(request.getSellerId())
                .adId(request.getAdId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();
        Review saved = reviewRepository.save(review);
        updateSellerRating(request.getSellerId());
        return mapToResponse(saved);
    }

    @Override
    public List<ReviewResponse> getReviewsBySeller(String sellerId) {
        return reviewRepository.findBySellerId(sellerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByReviewer(String reviewerId) {
        return reviewRepository.findByReviewerId(reviewerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByAd(String adId) {
        return reviewRepository.findByAdId(adId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReview(String id, String userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        if (!review.getReviewerId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to delete this review");
        }
        reviewRepository.deleteById(id);
        updateSellerRating(review.getSellerId());
    }

    private void updateSellerRating(String sellerId) {
        List<Review> reviews = reviewRepository.findBySellerId(sellerId);
        if (!reviews.isEmpty()) {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            userRepository.findById(sellerId).ifPresent(user -> {
                user.setAvgRating(avg);
                userRepository.save(user);
            });
        }
    }

    private ReviewResponse mapToResponse(Review review) {
        String reviewerName = userRepository.findById(review.getReviewerId())
                .map(User::getName).orElse("Unknown");
        return ReviewResponse.builder()
                .id(review.getId())
                .reviewerId(review.getReviewerId())
                .reviewerName(reviewerName)
                .sellerId(review.getSellerId())
                .adId(review.getAdId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
