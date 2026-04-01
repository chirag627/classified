package com.classified.app.service;

import com.classified.app.dto.request.CreateAdRequest;
import com.classified.app.dto.request.UpdateAdRequest;
import com.classified.app.dto.response.AdResponse;
import com.classified.app.dto.response.PagedResponse;
import com.classified.app.exception.BadRequestException;
import com.classified.app.exception.ResourceNotFoundException;
import com.classified.app.exception.UnauthorizedException;
import com.classified.app.model.Ad;
import com.classified.app.model.Category;
import com.classified.app.model.Location;
import com.classified.app.model.User;
import com.classified.app.repository.AdRepository;
import com.classified.app.repository.CategoryRepository;
import com.classified.app.repository.FavoriteRepository;
import com.classified.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FavoriteRepository favoriteRepository;

    @Override
    public AdResponse createAd(CreateAdRequest request, String userId) {
        Location location = Location.builder()
                .city(request.getCity())
                .state(request.getState())
                .lat(request.getLat())
                .lng(request.getLng())
                .build();

        Ad ad = Ad.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .categoryId(request.getCategoryId())
                .subcategoryId(request.getSubcategoryId())
                .price(request.getPrice())
                .negotiable(request.isNegotiable())
                .hidePrice(request.isHidePrice())
                .images(request.getImages())
                .location(location)
                .tags(request.getTags())
                .condition(request.getCondition())
                .userId(userId)
                .status(Ad.AdStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        return mapToResponse(adRepository.save(ad), userId);
    }

    @Override
    public AdResponse updateAd(String id, UpdateAdRequest request, String userId) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", id));
        if (!ad.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update this ad");
        }
        if (request.getTitle() != null) ad.setTitle(request.getTitle());
        if (request.getDescription() != null) ad.setDescription(request.getDescription());
        if (request.getCategoryId() != null) ad.setCategoryId(request.getCategoryId());
        if (request.getSubcategoryId() != null) ad.setSubcategoryId(request.getSubcategoryId());
        if (request.getPrice() != null) ad.setPrice(request.getPrice());
        if (request.getNegotiable() != null) ad.setNegotiable(request.getNegotiable());
        if (request.getHidePrice() != null) ad.setHidePrice(request.getHidePrice());
        if (request.getImages() != null) ad.setImages(request.getImages());
        if (request.getTags() != null) ad.setTags(request.getTags());
        if (request.getCondition() != null) ad.setCondition(request.getCondition());
        if (request.getCity() != null || request.getState() != null) {
            Location loc = ad.getLocation() != null ? ad.getLocation() : new Location();
            if (request.getCity() != null) loc.setCity(request.getCity());
            if (request.getState() != null) loc.setState(request.getState());
            if (request.getLat() != null) loc.setLat(request.getLat());
            if (request.getLng() != null) loc.setLng(request.getLng());
            ad.setLocation(loc);
        }
        ad.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(adRepository.save(ad), userId);
    }

    @Override
    public void deleteAd(String id, String userId) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", id));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        boolean isAdmin = user.getRoles().contains("ADMIN");
        if (!ad.getUserId().equals(userId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this ad");
        }
        adRepository.deleteById(id);
    }

    @Override
    public AdResponse getAdById(String id) {
        cleanupExpiredBoosts();
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", id));
        return mapToResponse(ad, null);
    }

    @Override
    public AdResponse boostAd(String id, String userId, Double amount, Integer days) {
        if (amount == null || amount <= 0) {
            throw new BadRequestException("Boost amount must be greater than 0");
        }
        if (days == null || days <= 0 || days > 30) {
            throw new BadRequestException("Boost duration must be between 1 and 30 days");
        }

        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", id));
        if (!ad.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to boost this ad");
        }
        if (ad.getStatus() != Ad.AdStatus.ACTIVE) {
            throw new BadRequestException("Only active ads can be boosted");
        }

        LocalDateTime now = LocalDateTime.now();
        ad.setFeatured(true);
        ad.setBoostAmount(amount);
        ad.setBoostDays(days);
        ad.setBoostExpiry(now.plusDays(days));
        ad.setUpdatedAt(now);
        return mapToResponse(adRepository.save(ad), userId);
    }

    @Override
    public PagedResponse<AdResponse> getAllAds(int page, int size) {
        cleanupExpiredBoosts();
        Pageable pageable = PageRequest.of(page, size, getBoostedSort());
        Page<Ad> ads = adRepository.findByStatus(Ad.AdStatus.ACTIVE, pageable);
        return buildPagedResponse(ads, null);
    }

    @Override
    public PagedResponse<AdResponse> getAdsByCategory(String categoryId, int page, int size) {
        cleanupExpiredBoosts();
        Pageable pageable = PageRequest.of(page, size, getBoostedSort());
        Page<Ad> ads = adRepository.findByCategoryIdAndStatus(categoryId, Ad.AdStatus.ACTIVE, pageable);
        return buildPagedResponse(ads, null);
    }

    @Override
    public PagedResponse<AdResponse> searchAds(String keyword, String categoryId, int page, int size) {
        cleanupExpiredBoosts();
        Pageable pageable = PageRequest.of(page, size, getBoostedSort());
        Page<Ad> ads;
        if (categoryId != null && !categoryId.isBlank()) {
            ads = adRepository.searchByKeywordAndCategory(categoryId, keyword, pageable);
        } else {
            ads = adRepository.searchByKeyword(keyword, pageable);
        }
        return buildPagedResponse(ads, null);
    }

    @Override
    public List<AdResponse> getFeaturedAds() {
        cleanupExpiredBoosts();
        return adRepository.findByFeaturedTrueAndStatus(Ad.AdStatus.ACTIVE).stream()
                .sorted(Comparator.comparing(Ad::getBoostExpiry,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Ad::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ad -> mapToResponse(ad, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdResponse> getAdsByUser(String userId) {
        cleanupExpiredBoosts();
        return adRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .sorted(Comparator.comparing(Ad::isFeatured).reversed()
                        .thenComparing(Ad::getBoostExpiry, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Ad::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ad -> mapToResponse(ad, userId))
                .collect(Collectors.toList());
    }

    @Override
    public AdResponse approveAd(String id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", id));
        ad.setStatus(Ad.AdStatus.ACTIVE);
        ad.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(adRepository.save(ad), null);
    }

    @Override
    public AdResponse rejectAd(String id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ad", "id", id));
        ad.setStatus(Ad.AdStatus.REJECTED);
        ad.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(adRepository.save(ad), null);
    }

    @Override
    public PagedResponse<AdResponse> getAdsByStatus(String status, int page, int size) {
        cleanupExpiredBoosts();
        Pageable pageable = PageRequest.of(page, size, getBoostedSort());
        Ad.AdStatus adStatus = Ad.AdStatus.valueOf(status.toUpperCase());
        Page<Ad> ads = adRepository.findByStatus(adStatus, pageable);
        return buildPagedResponse(ads, null);
    }

    @Override
    public void incrementViews(String id) {
        adRepository.findById(id).ifPresent(ad -> {
            ad.setViews(ad.getViews() + 1);
            adRepository.save(ad);
        });
    }

    private AdResponse mapToResponse(Ad ad, String currentUserId) {
        String categoryName = null;
        if (ad.getCategoryId() != null) {
            categoryName = categoryRepository.findById(ad.getCategoryId())
                    .map(Category::getName).orElse(null);
        }
        String userName = null;
        String sellerPhone = null;
        if (ad.getUserId() != null) {
            User seller = userRepository.findById(ad.getUserId()).orElse(null);
            if (seller != null) {
                userName = seller.getName();
                sellerPhone = seller.getPhone();
            }
        }
        boolean favorited = currentUserId != null &&
                favoriteRepository.existsByUserIdAndAdId(currentUserId, ad.getId());
        long favoriteCount = favoriteRepository.countByAdId(ad.getId());
        boolean boosted = ad.isFeatured() && ad.getBoostExpiry() != null && ad.getBoostExpiry().isAfter(LocalDateTime.now());

        return AdResponse.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .description(ad.getDescription())
                .categoryId(ad.getCategoryId())
                .categoryName(categoryName)
                .subcategoryId(ad.getSubcategoryId())
                .price(ad.getPrice())
                .negotiable(ad.isNegotiable())
                .hidePrice(ad.isHidePrice())
                .images(ad.getImages())
                .location(ad.getLocation())
                .tags(ad.getTags())
                .condition(ad.getCondition())
                .userId(ad.getUserId())
                .userName(userName)
                .sellerPhone(sellerPhone)
                .status(ad.getStatus())
                .featured(ad.isFeatured())
                .boosted(boosted)
                .boostAmount(ad.getBoostAmount())
                .boostDays(ad.getBoostDays())
                .boostExpiry(ad.getBoostExpiry())
                .views(ad.getViews())
                .createdAt(ad.getCreatedAt())
                .updatedAt(ad.getUpdatedAt())
                .expiresAt(ad.getExpiresAt())
                .favorited(favorited)
                .favoriteCount(favoriteCount)
                .build();
    }

    private PagedResponse<AdResponse> buildPagedResponse(Page<Ad> page, String currentUserId) {
        List<AdResponse> content = page.getContent().stream()
                .map(ad -> mapToResponse(ad, currentUserId))
                .collect(Collectors.toList());
        return PagedResponse.<AdResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    private Sort getBoostedSort() {
        return Sort.by(
                Sort.Order.desc("featured"),
                Sort.Order.desc("boostExpiry"),
                Sort.Order.desc("createdAt")
        );
    }

    private void cleanupExpiredBoosts() {
        List<Ad> expiredBoosts = adRepository.findByFeaturedTrueAndBoostExpiryBefore(LocalDateTime.now());
        if (expiredBoosts.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        expiredBoosts.forEach(ad -> {
            ad.setFeatured(false);
            ad.setUpdatedAt(now);
        });
        adRepository.saveAll(expiredBoosts);
    }
}
