package com.classified.app.repository;

import com.classified.app.model.Ad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AdRepository extends MongoRepository<Ad, String> {
    Page<Ad> findByStatus(Ad.AdStatus status, Pageable pageable);
    Page<Ad> findByCategoryIdAndStatus(String categoryId, Ad.AdStatus status, Pageable pageable);
    Page<Ad> findByUserIdAndStatus(String userId, Ad.AdStatus status, Pageable pageable);
    List<Ad> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Ad> findByFeaturedTrueAndStatus(Ad.AdStatus status);

    @Query("{ 'status': 'ACTIVE', $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    Page<Ad> searchByKeyword(String keyword, Pageable pageable);

    @Query("{ 'status': 'ACTIVE', 'categoryId': ?0, $or: [ { 'title': { $regex: ?1, $options: 'i' } }, { 'description': { $regex: ?1, $options: 'i' } } ] }")
    Page<Ad> searchByKeywordAndCategory(String categoryId, String keyword, Pageable pageable);

    long countByStatus(Ad.AdStatus status);
    long countByUserId(String userId);
}
