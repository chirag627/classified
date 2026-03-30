package com.classified.app.repository;

import com.classified.app.model.Favorite;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends MongoRepository<Favorite, String> {
    List<Favorite> findByUserId(String userId);
    Optional<Favorite> findByUserIdAndAdId(String userId, String adId);
    boolean existsByUserIdAndAdId(String userId, String adId);
    void deleteByUserIdAndAdId(String userId, String adId);
    long countByAdId(String adId);
}
