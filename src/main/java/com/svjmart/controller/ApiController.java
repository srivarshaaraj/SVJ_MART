package com.svjmart.controller;

import com.svjmart.dto.AiChatRequestDto;
import com.svjmart.dto.AiChatResponseDto;
import com.svjmart.dto.ProductSummaryDto;
import com.svjmart.entity.User;
import com.svjmart.service.CartService;
import com.svjmart.service.NewsletterService;
import com.svjmart.service.ProductService;
import com.svjmart.service.UserService;
import com.svjmart.service.WishlistService;
import com.svjmart.service.ai.AiShoppingAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ProductService productService;
    private final AiShoppingAssistantService aiShoppingAssistantService;
    private final NewsletterService newsletterService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final UserService userService;

    public ApiController(ProductService productService,
                         AiShoppingAssistantService aiShoppingAssistantService,
                         NewsletterService newsletterService,
                         CartService cartService,
                         WishlistService wishlistService,
                         UserService userService) {
        this.productService = productService;
        this.aiShoppingAssistantService = aiShoppingAssistantService;
        this.newsletterService = newsletterService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    @GetMapping("/search/suggestions")
    public ResponseEntity<List<ProductSummaryDto>> getSuggestions(@RequestParam("q") String query) {
        return ResponseEntity.ok(productService.getQuickSuggestions(query));
    }

    @PostMapping("/ai/chat")
    public ResponseEntity<AiChatResponseDto> chatWithAssistant(@RequestBody AiChatRequestDto request) {
        AiChatResponseDto response = aiShoppingAssistantService.processUserQuery(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/newsletter/subscribe")
    public ResponseEntity<Map<String, Object>> subscribeNewsletter(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isNew = newsletterService.subscribe(email);
            response.put("success", true);
            response.put("message", isNew ? "Thank you for subscribing to SVJ MART weekly grocery deals!" : "You are already subscribed to our newsletter!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/cart/count")
    public ResponseEntity<Map<String, Integer>> getCartCount(Authentication authentication) {
        int count = 0;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            count = cartService.getCartItemCount(user);
        }
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/wishlist/count")
    public ResponseEntity<Map<String, Integer>> getWishlistCount(Authentication authentication) {
        int count = 0;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            count = wishlistService.getWishlistCount(user);
        }
        return ResponseEntity.ok(Map.of("count", count));
    }
}
