package com.svjmart.controller;

import com.svjmart.entity.Category;
import com.svjmart.entity.Product;
import com.svjmart.entity.User;
import com.svjmart.service.CategoryService;
import com.svjmart.service.ProductService;
import com.svjmart.service.ReviewService;
import com.svjmart.service.UserService;
import com.svjmart.service.ai.AiRecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final AiRecommendationService aiRecommendationService;
    private final UserService userService;

    public HomeController(ProductService productService,
                          CategoryService categoryService,
                          ReviewService reviewService,
                          AiRecommendationService aiRecommendationService,
                          UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.aiRecommendationService = aiRecommendationService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        User user = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            user = userService.findByEmail(authentication.getName()).orElse(null);
        }

        List<Product> allProducts = productService.getAllProducts();

        // 1. Featured / Daily Essentials
        List<Product> dailyEssentials = productService.getFeaturedProducts().stream()
                .limit(8)
                .collect(Collectors.toList());

        // 2. Snacks & Beverages
        List<Product> snacksAndBeverages = allProducts.stream()
                .filter(p -> p.getCategory() != null &&
                        ("snacks-munchies".equals(p.getCategory().getSlug()) ||
                         "beverages".equals(p.getCategory().getSlug())))
                .limit(8)
                .collect(Collectors.toList());

        // 3. Fresh Picks (Produce & Dairy)
        List<Product> freshPicks = allProducts.stream()
                .filter(p -> p.isPerishable() && p.isInStock())
                .limit(8)
                .collect(Collectors.toList());

        // 4. Cooking Corner (Grains, Oils, Spices)
        List<Product> cookingCorner = allProducts.stream()
                .filter(p -> p.getCategory() != null &&
                        ("rice-grains-staples".equals(p.getCategory().getSlug()) ||
                         "cooking-oils-ingredients".equals(p.getCategory().getSlug()) ||
                         "spices-masalas".equals(p.getCategory().getSlug())))
                .limit(8)
                .collect(Collectors.toList());

        // 5. Household & Cleaning
        List<Product> householdEssentials = allProducts.stream()
                .filter(p -> p.getCategory() != null &&
                        ("cleaning-products".equals(p.getCategory().getSlug()) ||
                         "cooking-and-kitchen-essentials".equals(p.getCategory().getSlug()) ||
                         "home-household-essentials".equals(p.getCategory().getSlug())))
                .limit(8)
                .collect(Collectors.toList());

        // 6. AI Personalized Recommendations
        List<Product> aiRecommendations = aiRecommendationService.getPersonalizedRecommendations(user, 8);

        // 7. Bestsellers
        List<Product> bestsellers = productService.getBestsellerProducts().stream()
                .limit(8)
                .collect(Collectors.toList());

        // 8. Categories
        List<Category> categories = categoryService.getActiveCategories();

        model.addAttribute("categories", categories);
        model.addAttribute("dailyEssentials", dailyEssentials);
        model.addAttribute("snacksAndBeverages", snacksAndBeverages);
        model.addAttribute("freshPicks", freshPicks);
        model.addAttribute("cookingCorner", cookingCorner);
        model.addAttribute("householdEssentials", householdEssentials);
        model.addAttribute("aiRecommendations", aiRecommendations);
        model.addAttribute("bestsellers", bestsellers);
        model.addAttribute("demoReviews", reviewService.getAllReviews().stream().limit(6).collect(Collectors.toList()));

        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
