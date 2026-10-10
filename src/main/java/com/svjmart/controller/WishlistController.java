package com.svjmart.controller;

import com.svjmart.entity.User;
import com.svjmart.entity.Wishlist;
import com.svjmart.service.CartService;
import com.svjmart.service.UserService;
import com.svjmart.service.WishlistService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final CartService cartService;
    private final UserService userService;

    public WishlistController(WishlistService wishlistService, CartService cartService, UserService userService) {
        this.wishlistService = wishlistService;
        this.cartService = cartService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }

    @GetMapping
    public String viewWishlist(Authentication authentication, Model model) {
        User user = getAuthenticatedUser(authentication);
        Wishlist wishlist = wishlistService.getOrCreateWishlist(user);
        model.addAttribute("wishlist", wishlist);
        return "buyer/wishlist";
    }

    @PostMapping("/toggle/{productId}")
    public String toggleWishlist(@PathVariable("productId") Long productId,
                                 @RequestHeader(value = "Referer", required = false) String referer,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        boolean added = wishlistService.toggleWishlist(user, productId);
        if (added) {
            redirectAttributes.addFlashAttribute("successMessage", "Item saved to your Wishlist!");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "Item removed from your Wishlist.");
        }
        return referer != null ? "redirect:" + referer : "redirect:/wishlist";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromWishlist(@PathVariable("productId") Long productId,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        wishlistService.removeFromWishlist(user, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from Wishlist.");
        return "redirect:/wishlist";
    }

    @PostMapping("/move-to-cart/{productId}")
    public String moveToCart(@PathVariable("productId") Long productId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        try {
            cartService.addToCart(user, productId, 1);
            wishlistService.removeFromWishlist(user, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Item moved to your shopping basket!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/wishlist";
    }
}
