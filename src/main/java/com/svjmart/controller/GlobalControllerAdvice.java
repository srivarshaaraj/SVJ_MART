package com.svjmart.controller;

import com.svjmart.entity.Category;
import com.svjmart.entity.User;
import com.svjmart.service.CartService;
import com.svjmart.service.CategoryService;
import com.svjmart.service.UserService;
import com.svjmart.service.WishlistService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CategoryService categoryService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final UserService userService;

    public GlobalControllerAdvice(CategoryService categoryService,
                                  CartService cartService,
                                  WishlistService wishlistService,
                                  UserService userService) {
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    @ModelAttribute("navCategories")
    public List<Category> getNavCategories() {
        return categoryService.getActiveCategories();
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return userService.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    @ModelAttribute("cartCount")
    public int getCartCount(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return user != null ? cartService.getCartItemCount(user) : 0;
    }

    @ModelAttribute("wishlistCount")
    public int getWishlistCount(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return user != null ? wishlistService.getWishlistCount(user) : 0;
    }
}
