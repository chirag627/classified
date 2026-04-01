package com.classified.app.controller;

import com.classified.app.dto.response.AdResponse;
import com.classified.app.dto.response.PagedResponse;
import com.classified.app.model.Category;
import com.classified.app.service.AdService;
import com.classified.app.service.CategoryService;
import com.classified.app.service.FavoriteService;
import com.classified.app.service.MessageService;
import com.classified.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final AdService adService;
    private final CategoryService categoryService;
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final MessageService messageService;

    @GetMapping({"/", "/index"})
    public String home(Model model) {
        List<Category> categories = categoryService.getTopLevelCategories();
        List<AdResponse> featuredAds = adService.getFeaturedAds();
        PagedResponse<AdResponse> recentAds = adService.getAllAds(0, 8);
        model.addAttribute("categories", categories);
        model.addAttribute("featuredAds", featuredAds);
        model.addAttribute("recentAds", recentAds.getContent());
        return "index";
    }

    @GetMapping("/ads")
    public String listAds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        List<Category> categories = categoryService.getTopLevelCategories();
        PagedResponse<AdResponse> ads;
        if (keyword != null && !keyword.isBlank()) {
            ads = adService.searchAds(keyword, categoryId, page, size);
        } else if (categoryId != null && !categoryId.isBlank()) {
            ads = adService.getAdsByCategory(categoryId, page, size);
        } else {
            ads = adService.getAllAds(page, size);
        }
        model.addAttribute("categories", categories);
        model.addAttribute("ads", ads);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        return "ads/list";
    }

    @GetMapping("/ads/{id}")
    public String adDetail(@PathVariable String id, Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {
        AdResponse ad = adService.getAdById(id);
        adService.incrementViews(id);
        List<AdResponse> similarAds = adService.getAdsByCategory(ad.getCategoryId(), 0, 4).getContent();
        model.addAttribute("ad", ad);
        model.addAttribute("similarAds", similarAds);
        if (userDetails != null) {
            boolean favorited = favoriteService.isFavorited(userDetails.getUsername(), id);
            model.addAttribute("favorited", favorited);
        }
        return "ads/detail";
    }

    @GetMapping("/ads/create")
    public String createAdForm(Model model) {
        List<Category> categories = categoryService.getTopLevelCategories();
        model.addAttribute("categories", categories);
        return "ads/create";
    }

    @GetMapping("/ads/{id}/edit")
    public String editAdForm(@PathVariable String id, Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        AdResponse ad = adService.getAdById(id);
        List<Category> categories = categoryService.getTopLevelCategories();
        model.addAttribute("ad", ad);
        model.addAttribute("categories", categories);
        return "ads/edit";
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/user/dashboard")
    public String userDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        String userId = userDetails.getUsername();
        List<AdResponse> myAds = adService.getAdsByUser(userId);
        List<AdResponse> favorites = favoriteService.getUserFavorites(userId);
        model.addAttribute("myAds", myAds);
        model.addAttribute("favorites", favorites);
        model.addAttribute("user", userService.getCurrentUser(userId));
        model.addAttribute("unreadMessageCount", messageService.getUnreadCount(userId));
        return "user/dashboard";
    }

    @GetMapping("/user/profile")
    public String profilePage(Model model, @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) Boolean emailVerified,
                              @RequestParam(required = false) Boolean emailError) {
        if (userDetails == null) return "redirect:/auth/login";
        model.addAttribute("user", userService.getCurrentUser(userDetails.getUsername()));
        if (Boolean.TRUE.equals(emailVerified)) model.addAttribute("emailVerified", true);
        if (Boolean.TRUE.equals(emailError)) model.addAttribute("emailError", true);
        return "user/profile";
    }

    @GetMapping("/user/favorites")
    public String favoritesPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        List<AdResponse> favorites = favoriteService.getUserFavorites(userDetails.getUsername());
        model.addAttribute("favorites", favorites);
        return "user/favorites";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/ads")
    public String adminAds(Model model) {
        PagedResponse<AdResponse> pendingAds = adService.getAdsByStatus("PENDING", 0, 20);
        model.addAttribute("pendingAds", pendingAds.getContent());
        PagedResponse<AdResponse> activeAds = adService.getAdsByStatus("ACTIVE", 0, 20);
        model.addAttribute("activeAds", activeAds.getContent());
        return "admin/ads";
    }

    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        return "admin/reports";
    }

    @GetMapping("/chat/messages")
    public String messagesPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        return "chat/messages";
    }
}
