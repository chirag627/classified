package com.classified.app.service;

import com.classified.app.dto.request.CreateAdRequest;
import com.classified.app.dto.response.AdResponse;
import com.classified.app.exception.ResourceNotFoundException;
import com.classified.app.model.Ad;
import com.classified.app.model.Category;
import com.classified.app.model.User;
import com.classified.app.repository.AdRepository;
import com.classified.app.repository.CategoryRepository;
import com.classified.app.repository.FavoriteRepository;
import com.classified.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdServiceTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private AdServiceImpl adService;

    private Ad testAd;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user1")
                .name("Test User")
                .email("test@test.com")
                .roles(Set.of("USER"))
                .build();

        testCategory = Category.builder()
                .id("cat1")
                .name("Electronics")
                .slug("electronics")
                .build();

        testAd = Ad.builder()
                .id("ad1")
                .title("Test iPhone")
                .description("Great iPhone for sale")
                .categoryId("cat1")
                .price(999.99)
                .userId("user1")
                .status(Ad.AdStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAdById_existingAd_returnsAdResponse() {
        when(adRepository.findById("ad1")).thenReturn(Optional.of(testAd));
        when(categoryRepository.findById("cat1")).thenReturn(Optional.of(testCategory));
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(favoriteRepository.existsByUserIdAndAdId(any(), any())).thenReturn(false);
        when(favoriteRepository.countByAdId("ad1")).thenReturn(5L);

        AdResponse result = adService.getAdById("ad1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("ad1");
        assertThat(result.getTitle()).isEqualTo("Test iPhone");
        assertThat(result.getCategoryName()).isEqualTo("Electronics");
        assertThat(result.getUserName()).isEqualTo("Test User");
        assertThat(result.getFavoriteCount()).isEqualTo(5L);
    }

    @Test
    void getAdById_nonExistingAd_throwsException() {
        when(adRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adService.getAdById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAd_validRequest_returnsAdResponse() {
        CreateAdRequest request = new CreateAdRequest();
        request.setTitle("New Ad");
        request.setDescription("Description");
        request.setCategoryId("cat1");
        request.setPrice(500.0);

        Ad savedAd = Ad.builder()
                .id("newAd1")
                .title("New Ad")
                .description("Description")
                .categoryId("cat1")
                .price(500.0)
                .userId("user1")
                .status(Ad.AdStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(adRepository.save(any(Ad.class))).thenReturn(savedAd);
        when(categoryRepository.findById("cat1")).thenReturn(Optional.of(testCategory));
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(favoriteRepository.existsByUserIdAndAdId(any(), any())).thenReturn(false);
        when(favoriteRepository.countByAdId(any())).thenReturn(0L);

        AdResponse result = adService.createAd(request, "user1");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Ad");
        assertThat(result.getStatus()).isEqualTo(Ad.AdStatus.PENDING);
        verify(adRepository, times(1)).save(any(Ad.class));
    }

    @Test
    void getAllAds_returnsPagedResponse() {
        Page<Ad> page = new PageImpl<>(List.of(testAd));
        when(adRepository.findByStatus(eq(Ad.AdStatus.ACTIVE), any(Pageable.class))).thenReturn(page);
        when(categoryRepository.findById("cat1")).thenReturn(Optional.of(testCategory));
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(favoriteRepository.existsByUserIdAndAdId(any(), any())).thenReturn(false);
        when(favoriteRepository.countByAdId(any())).thenReturn(0L);

        var result = adService.getAllAds(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void approveAd_pendingAd_setsStatusToActive() {
        testAd.setStatus(Ad.AdStatus.PENDING);
        Ad approvedAd = Ad.builder()
                .id("ad1")
                .title(testAd.getTitle())
                .status(Ad.AdStatus.ACTIVE)
                .userId("user1")
                .categoryId("cat1")
                .build();

        when(adRepository.findById("ad1")).thenReturn(Optional.of(testAd));
        when(adRepository.save(any(Ad.class))).thenReturn(approvedAd);
        when(categoryRepository.findById(any())).thenReturn(Optional.of(testCategory));
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(favoriteRepository.existsByUserIdAndAdId(any(), any())).thenReturn(false);
        when(favoriteRepository.countByAdId(any())).thenReturn(0L);

        AdResponse result = adService.approveAd("ad1");

        assertThat(result.getStatus()).isEqualTo(Ad.AdStatus.ACTIVE);
    }

    @Test
    void incrementViews_existingAd_incrementsViewCount() {
        testAd.setViews(10);
        when(adRepository.findById("ad1")).thenReturn(Optional.of(testAd));
        when(adRepository.save(any(Ad.class))).thenReturn(testAd);

        adService.incrementViews("ad1");

        verify(adRepository, times(1)).save(argThat(ad -> ad.getViews() == 11));
    }
}
