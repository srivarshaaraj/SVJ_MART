package com.svjmart.service.ai;

import com.svjmart.dto.AiChatRequestDto;
import com.svjmart.dto.AiChatResponseDto;
import com.svjmart.dto.ProductSummaryDto;
import com.svjmart.entity.Product;
import com.svjmart.repository.ProductRepository;
import com.svjmart.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Conversational AI Shopping Assistant & Meal-Prep Basket Optimizer.
 * Core intelligent agent for B.Tech AI & Data Science Capstone.
 */
@Service
public class AiShoppingAssistantService {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public AiShoppingAssistantService(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    public AiChatResponseDto processUserQuery(AiChatRequestDto request) {
        String query = request.getQuery() != null ? request.getQuery().toLowerCase().trim() : "";
        BigDecimal budget = request.getBudget();

        // Automatically extract budget from natural language if not supplied directly
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:budget|under|below|for|within)\\s*(?:₹|rs\\.?|inr)?\\s*(\\d+)").matcher(query);
            if (m.find()) {
                try {
                    budget = new BigDecimal(m.group(1));
                } catch (Exception ignored) {
                }
            }
        }

        // Check if query is Budget-driven
        if (budget != null && budget.compareTo(BigDecimal.ZERO) > 0) {
            return generateBudgetOptimizedBasket(budget, request.getDietPreference(), request.getHouseholdType());
        }

        // Check if query matches a culinary recipe or household kit
        if (query.contains("sambar")) {
            return buildRecipeBasket("South Indian Sambar Kit",
                    "Here are the authentic ingredients for delicious homemade Sambar, curated from our staples and spice aisles.",
                    List.of("toor dal", "sambar powder", "mustard seeds", "tomato", "ghee"),
                    "RECIPE_BASKET");
        }

        if (query.contains("biryani")) {
            return buildRecipeBasket("Special Royal Biryani Kit",
                    "Everything you need for an aromatic biryani: aged basmati rice, authentic biryani masala, pure cow ghee, and fresh aromatics.",
                    List.of("basmati", "biryani masala", "cow ghee", "ginger", "paneer"),
                    "RECIPE_BASKET");
        }

        if (query.contains("tea") || query.contains("chai")) {
            return buildRecipeBasket("Classic Indian Masala Chai Kit",
                    "Brew the perfect cup of morning chai with premium tea leaves, fresh dairy milk, sugar, and whole cardamom.",
                    List.of("tea", "milk", "sugar", "cardamom", "rusk"),
                    "RECIPE_BASKET");
        }

        if (query.contains("coffee")) {
            return buildRecipeBasket("Filter & Instant Coffee Combo",
                    "Rich aromatic coffee beans, fresh milk, and sweeteners for your daily caffeine boost.",
                    List.of("coffee", "milk", "sugar", "cookies"),
                    "RECIPE_BASKET");
        }

        if (query.contains("breakfast") || query.contains("idli") || query.contains("dosa")) {
            return buildRecipeBasket("Quick South Indian Breakfast Kit",
                    "Fresh ready-to-cook batter, golden ghee, chutney accompaniments, and wholesome oats for rapid morning prep.",
                    List.of("batter", "ghee", "oats", "bread", "butter"),
                    "RECIPE_BASKET");
        }

        if (query.contains("cleaning") || query.contains("bathroom") || query.contains("wash")) {
            return buildRecipeBasket("Complete Home & Bathroom Deep Cleaning Kit",
                    "Powerful hygiene supplies: disinfectant floor cleaners, toilet cleaner, dishwash gel, and heavy-duty sponges.",
                    List.of("floor cleaner", "toilet cleaner", "dishwash", "scrub", "garbage"),
                    "HOUSEHOLD_KIT");
        }

        if (query.contains("pooja") || query.contains("diwali") || query.contains("temple")) {
            return buildRecipeBasket("Pious Daily Pooja Essentials Kit",
                    "Pure fragrant incense sticks, Bhimseni camphor, cotton ghee wicks, and pooja oil for daily devotion.",
                    List.of("agarbatti", "camphor", "wick", "pooja oil"),
                    "POOJA_KIT");
        }

        if (query.contains("healthy") || query.contains("protein") || query.contains("diet") || query.contains("gym")) {
            return buildHealthBasket("High-Nutrition & Wellness Essentials",
                    "Curated low-glycemic staples, high-protein pulses, oats, green tea, and nutrient-dense dry fruits.");
        }

        if (query.contains("snack") || query.contains("munch") || query.contains("party") || query.contains("movie")) {
            return buildSnackPack();
        }

        // Generic intelligent keyword search across catalogue
        return buildGeneralSearchAssist(query);
    }

    private AiChatResponseDto generateBudgetOptimizedBasket(BigDecimal budget, String dietPref, String householdType) {
        AiChatResponseDto response = new AiChatResponseDto();
        response.setIntentType("BUDGET_OPTIMIZER");
        response.setRationale("Algorithmic Budget Knapsack Optimizer: Curated essential staples, dairy, cooking oil, and household necessities fitting within ₹" + budget.toPlainString());
        response.setReplyText("I have assembled an optimal basket of everyday essentials tailored for your ₹" + budget.toPlainString() + " budget limit.");

        List<Product> available = productRepository.findAll().stream()
                .filter(Product::isInStock)
                .sorted(Comparator.comparingDouble(Product::getAverageRating).reversed())
                .collect(Collectors.toList());

        BigDecimal currentTotal = BigDecimal.ZERO;
        Set<String> includedCategories = new HashSet<>();

        // Priority greedy knapsack: take 1 item from distinct essential categories
        for (Product p : available) {
            if ("VEG".equalsIgnoreCase(dietPref) && !p.isVegetarian()) {
                continue;
            }

            BigDecimal nextTotal = currentTotal.add(p.getPrice());
            if (nextTotal.compareTo(budget) <= 0) {
                String catSlug = p.getCategory() != null ? p.getCategory().getSlug() : "";
                // ensure category variety
                if (includedCategories.size() < 6 || !includedCategories.contains(catSlug)) {
                    response.addProduct(productService.mapToSummaryDto(p));
                    currentTotal = nextTotal;
                    includedCategories.add(catSlug);
                }
            }
        }

        return response;
    }

    private AiChatResponseDto buildRecipeBasket(String title, String rationale, List<String> keywords, String intentType) {
        AiChatResponseDto response = new AiChatResponseDto();
        response.setIntentType(intentType);
        response.setRationale(rationale);
        response.setReplyText("Here is your curated " + title + " containing all essential ingredients:");

        List<Product> all = productRepository.findAll().stream().filter(Product::isInStock).collect(Collectors.toList());

        for (String kw : keywords) {
            Optional<Product> match = all.stream()
                    .filter(p -> p.getName().toLowerCase().contains(kw) ||
                                 p.getBrand().toLowerCase().contains(kw) ||
                                 (p.getDescription() != null && p.getDescription().toLowerCase().contains(kw)))
                    .findFirst();

            match.ifPresent(p -> response.addProduct(productService.mapToSummaryDto(p)));
        }

        return response;
    }

    private AiChatResponseDto buildHealthBasket(String title, String rationale) {
        AiChatResponseDto response = new AiChatResponseDto();
        response.setIntentType("HEALTH_WELLNESS");
        response.setRationale(rationale);
        response.setReplyText("Here are top-rated health, high-fiber, and wellness staples selected for balanced nutrition:");

        List<Product> items = productRepository.findAll().stream()
                .filter(Product::isInStock)
                .filter(p -> {
                    String name = p.getName().toLowerCase();
                    return name.contains("oats") || name.contains("millet") || name.contains("green tea") ||
                            name.contains("honey") || name.contains("paneer") || name.contains("dal");
                })
                .limit(6)
                .collect(Collectors.toList());

        for (Product p : items) {
            response.addProduct(productService.mapToSummaryDto(p));
        }

        return response;
    }

    private AiChatResponseDto buildSnackPack() {
        AiChatResponseDto response = new AiChatResponseDto();
        response.setIntentType("SNACK_PACK");
        response.setRationale("Popular party and teatime snacks, crispy namkeen, cookies, and refreshing beverages.");
        response.setReplyText("Check out these crowd-favourite snacks and beverages for your break or gathering:");

        List<Product> items = productRepository.findAll().stream()
                .filter(Product::isInStock)
                .filter(p -> {
                    String cat = p.getCategory() != null ? p.getCategory().getSlug() : "";
                    return "snacks-munchies".equals(cat) || "beverages".equals(cat);
                })
                .limit(6)
                .collect(Collectors.toList());

        for (Product p : items) {
            response.addProduct(productService.mapToSummaryDto(p));
        }

        return response;
    }

    private AiChatResponseDto buildGeneralSearchAssist(String query) {
        AiChatResponseDto response = new AiChatResponseDto();
        response.setIntentType("CATALOG_ASSIST");
        response.setRationale("Semantic catalog matching based on keyword token: '" + query + "'");

        List<ProductSummaryDto> suggestions = productService.getQuickSuggestions(query);
        if (suggestions.isEmpty()) {
            response.setReplyText("I couldn't find exact matches for '" + query + "'. Here are popular daily essentials from our supermarket:");
            List<Product> popular = productRepository.findByIsBestsellerTrueAndIsAvailableTrue().stream().limit(5).collect(Collectors.toList());
            for (Product p : popular) {
                response.addProduct(productService.mapToSummaryDto(p));
            }
        } else {
            response.setReplyText("I found " + suggestions.size() + " supermarket products matching your query '" + query + "':");
            response.setSuggestedProducts(suggestions);
            for (ProductSummaryDto s : suggestions) {
                if (s.getPrice() != null) {
                    response.setTotalEstimatedPrice(response.getTotalEstimatedPrice().add(s.getPrice()));
                }
            }
        }

        return response;
    }
}
