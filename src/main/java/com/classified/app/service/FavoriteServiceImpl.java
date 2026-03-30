package com.classified.app.service;

import com.classified.app.dto.response.AdResponse;
import com.classified.app.exception.BadRequestException;
import com.classified.app.exception.ResourceNotFoundException;
import com.classified.app.model.Ad;
import com.classified.app.model.Favorite;
import com.classified.app.repository.AdRepository;
import com.classified.app.repository.CategoryRepository;
import com.classified.app.repository.FavoriteRepository;
import com.classified.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void addFavorite(String userId, String adId) {
        if (favoriteRepository.existsByUserIdAndAdId(userId, adId)) {
            throw new BadRequestException("Ad already in favorites");
        }
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .adId(adId)
                .createdAt(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(String userId, String adId) {
        favoriteRepository.deleteByUserIdAndAdId(userId, adId);
    }

    @Override
    public List<AdResponse> getUserFavorites(String userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(fav -> adRepository.findById(fav.getAdId()).orElse(null))
                .filter(ad -> ad != null)
                .map(ad -> mapAdToResponse(ad, userId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFavorited(String userId, String adId) {
        return favoriteRepository.existsByUserIdAndAdId(userId, adId);
    }

    @Override
    public long getFavoriteCount(String adId) {
        return favoriteRepository.countByAdId(adId);
    }

    private AdResponse mapAdToResponse(Ad ad, String currentUserId) {
        String categoryName = null;
        if (ad.getCategoryId() != null) {
            categoryName = categoryRepository.findById(ad.getCategoryId())
                    .map(c -> c.getName()).orElse(null);
        }
        String userName = null;
        if (ad.getUserId() != null) {
            userName = userRepository.findById(ad.getUserId())
                    .map(u -> u.getName()).orElse(null);
        }
        return AdResponse.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .description(ad.getDescription())
                .categoryId(ad.getCategoryId())
                .categoryName(categoryName)
                .price(ad.getPrice())
                .images(ad.getImages())
                .location(ad.getLocation())
                .userId(ad.getUserId())
                .userName(userName)
                .status(ad.getStatus())
                .views(ad.getViews())
                .createdAt(ad.getCreatedAt())
                .favorited(true)
                .favoriteCount(favoriteRepository.countByAdId(ad.getId()))
                .build();
    }
}
