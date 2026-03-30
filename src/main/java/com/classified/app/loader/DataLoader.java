package com.classified.app.loader;

import com.classified.app.model.*;
import com.classified.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void loadData() {
        if (categoryRepository.count() > 0) {
            log.info("Data already loaded, skipping...");
            return;
        }
        log.info("Loading initial data...");
        List<Category> categories = loadCategories();
        List<User> users = loadUsers();
        loadAds(categories, users);
        log.info("Initial data loaded successfully.");
    }

    private List<Category> loadCategories() {
        List<Category> categories = new ArrayList<>();
        String[][] catData = {
            {"Electronics", "electronics", "💻"},
            {"Vehicles", "vehicles", "🚗"},
            {"Real Estate", "real-estate", "🏠"},
            {"Jobs", "jobs", "💼"},
            {"Fashion", "fashion", "👗"},
            {"Services", "services", "🔧"},
            {"Sports", "sports", "⚽"},
            {"Home & Garden", "home-garden", "🌿"},
            {"Others", "others", "📦"}
        };
        for (String[] data : catData) {
            Category cat = Category.builder()
                    .name(data[0])
                    .slug(data[1])
                    .icon(data[2])
                    .active(true)
                    .build();
            categories.add(categoryRepository.save(cat));
        }
        return categories;
    }

    private List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        // Admin user
        User admin = User.builder()
                .name("Admin User")
                .email("admin@classified.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("555-0001")
                .city("New York")
                .state("NY")
                .roles(new HashSet<>(Arrays.asList("USER", "ADMIN")))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();
        users.add(userRepository.save(admin));

        // Regular users
        String[][] userData = {
            {"Alice Johnson", "alice@example.com", "555-0002", "Los Angeles", "CA"},
            {"Bob Smith", "bob@example.com", "555-0003", "Chicago", "IL"},
            {"Carol White", "carol@example.com", "555-0004", "Houston", "TX"},
            {"David Brown", "david@example.com", "555-0005", "Phoenix", "AZ"},
            {"Emma Davis", "emma@example.com", "555-0006", "Philadelphia", "PA"},
            {"Frank Miller", "frank@example.com", "555-0007", "San Antonio", "TX"},
            {"Grace Wilson", "grace@example.com", "555-0008", "San Diego", "CA"},
            {"Henry Moore", "henry@example.com", "555-0009", "Dallas", "TX"},
            {"Isabella Taylor", "isabella@example.com", "555-0010", "San Jose", "CA"}
        };
        for (String[] data : userData) {
            User user = User.builder()
                    .name(data[0])
                    .email(data[1])
                    .password(passwordEncoder.encode("password123"))
                    .phone(data[2])
                    .city(data[3])
                    .state(data[4])
                    .roles(new HashSet<>(Collections.singletonList("USER")))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .active(true)
                    .build();
            users.add(userRepository.save(user));
        }
        return users;
    }

    private void loadAds(List<Category> categories, List<User> users) {
        Map<String, String> catMap = new HashMap<>();
        for (Category c : categories) {
            catMap.put(c.getSlug(), c.getId());
        }

        Object[][] adData = {
            {"MacBook Pro 16\" M3", "Excellent condition MacBook Pro with M3 chip, 32GB RAM, 1TB SSD. Barely used.", "electronics", 2499.99, true, "NEW", "Los Angeles", "CA"},
            {"iPhone 15 Pro Max", "Like new iPhone 15 Pro Max 256GB, Space Black. Comes with original box.", "electronics", 1099.99, true, "USED", "Chicago", "IL"},
            {"Samsung 65\" 4K TV", "Samsung QLED 65 inch 4K Smart TV. Perfect working condition.", "electronics", 899.99, false, "USED", "Houston", "TX"},
            {"Toyota Camry 2022", "Well-maintained 2022 Toyota Camry with 25k miles. Single owner.", "vehicles", 28500.0, true, "USED", "Phoenix", "AZ"},
            {"Honda CBR 600RR", "2020 Honda CBR 600RR, red color, 12k miles. Excellent condition.", "vehicles", 9500.0, true, "USED", "Philadelphia", "PA"},
            {"Downtown Apartment 2BR", "Spacious 2-bedroom apartment in downtown. Great views, modern amenities.", "real-estate", 2500.0, true, "NEW", "New York", "NY"},
            {"Software Engineer Position", "Full-stack developer needed for growing startup. Remote friendly.", "jobs", 120000.0, false, "NEW", "San Francisco", "CA"},
            {"Nike Air Jordan 1 Retro", "Brand new Nike Air Jordan 1 Retro High OG, size 10. Never worn.", "fashion", 299.99, false, "NEW", "Dallas", "TX"},
            {"Plumbing Services", "Licensed plumber with 10+ years experience. Emergency services available.", "services", 75.0, true, "NEW", "San Diego", "CA"},
            {"Trek Mountain Bike", "Trek Marlin 7 mountain bike, barely used. All original components.", "sports", 649.99, true, "USED", "San Jose", "CA"},
            {"IKEA Dining Set", "IKEA Ekedalen dining table with 6 chairs. Good condition.", "home-garden", 350.0, true, "USED", "Los Angeles", "CA"},
            {"Sony PlayStation 5", "PS5 disc edition with 2 controllers and 5 games. Like new.", "electronics", 599.99, false, "USED", "Chicago", "IL"},
            {"Ford F-150 2021", "2021 Ford F-150 XLT, 4x4, 30k miles. One owner, no accidents.", "vehicles", 42000.0, true, "USED", "Houston", "TX"},
            {"Luxury Condo 3BR", "3-bedroom luxury condo with pool, gym access. Available immediately.", "real-estate", 4500.0, false, "NEW", "Miami", "FL"},
            {"Graphic Designer Freelance", "Experienced graphic designer available for logo, branding projects.", "jobs", 85.0, true, "NEW", "Remote", "NY"},
            {"Vintage Levi's Jeans", "Vintage 501 Levi's jeans, size 32x32. Great condition.", "fashion", 89.99, true, "USED", "Portland", "OR"},
            {"Garden Maintenance Service", "Professional garden maintenance, pruning, lawn care. Weekly/monthly.", "services", 120.0, true, "NEW", "Seattle", "WA"},
            {"Yoga Mat Bundle", "Premium yoga mat with blocks, strap, and bag. Like new.", "sports", 75.0, false, "USED", "Boston", "MA"},
            {"Vintage Record Player", "Working vintage Technics SL-1200 turntable. Needs new needle.", "others", 199.99, true, "USED", "Austin", "TX"},
            {"Dell XPS 15 Laptop", "Dell XPS 15 9530, i9, 32GB RAM, NVIDIA RTX 4060. Perfect condition.", "electronics", 1799.99, true, "USED", "Denver", "CO"}
        };

        int userIndex = 1;
        for (Object[] data : adData) {
            String catSlug = (String) data[2];
            String catId = catMap.getOrDefault(catSlug, categories.get(0).getId());
            User user = users.get(userIndex % users.size());
            userIndex++;

            Location location = Location.builder()
                    .city((String) data[6])
                    .state((String) data[7])
                    .build();

            Ad ad = Ad.builder()
                    .title((String) data[0])
                    .description((String) data[1])
                    .categoryId(catId)
                    .price((Double) data[3])
                    .negotiable((Boolean) data[4])
                    .condition(Ad.Condition.valueOf((String) data[5]))
                    .location(location)
                    .userId(user.getId())
                    .status(Ad.AdStatus.ACTIVE)
                    .featured(userIndex % 4 == 0)
                    .images(new ArrayList<>())
                    .tags(new ArrayList<>())
                    .views((long)(Math.random() * 200))
                    .createdAt(LocalDateTime.now().minusDays((long)(Math.random() * 30)))
                    .updatedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .build();
            adRepository.save(ad);
        }
    }
}
