package com.svjmart.controller;

import com.svjmart.entity.Cart;
import com.svjmart.entity.Coupon;
import com.svjmart.entity.User;
import com.svjmart.service.CartService;
import com.svjmart.service.CouponService;
import com.svjmart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CouponService couponService;
    private final UserService userService;

    public CartController(CartService cartService, CouponService couponService, UserService userService) {
        this.cartService = cartService;
        this.couponService = couponService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }

    @GetMapping
    public String viewCart(Authentication authentication, Model model, HttpSession session) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = cartService.getOrCreateCart(user);

        BigDecimal subtotal = cart.getSubtotal();
        String appliedCouponCode = (String) session.getAttribute("appliedCouponCode");
        BigDecimal discount = BigDecimal.ZERO;

        if (appliedCouponCode != null) {
            discount = couponService.calculateDiscount(appliedCouponCode, subtotal);
            if (discount.compareTo(BigDecimal.ZERO) == 0) {
                session.removeAttribute("appliedCouponCode");
                appliedCouponCode = null;
            }
        }

        BigDecimal deliveryFee = subtotal.compareTo(new BigDecimal("499.00")) >= 0 || subtotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : new BigDecimal("40.00");

        BigDecimal total = subtotal.subtract(discount).add(deliveryFee);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("appliedCouponCode", appliedCouponCode);
        model.addAttribute("discount", discount);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("finalTotal", total);
        model.addAttribute("activeCoupons", couponService.getActiveCoupons());

        return "buyer/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthenticatedUser(authentication);
            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to your basket successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthenticatedUser(authentication);
            cartService.updateQuantity(user, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Basket quantity updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable("productId") Long productId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthenticatedUser(authentication);
            cartService.removeFromCart(user, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from your basket.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        cartService.clearCart(user);
        redirectAttributes.addFlashAttribute("successMessage", "Your basket has been cleared.");
        return "redirect:/cart";
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam("couponCode") String couponCode,
                              Authentication authentication,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = cartService.getOrCreateCart(user);
        BigDecimal subtotal = cart.getSubtotal();

        Optional<Coupon> coupon = couponService.findValidCoupon(couponCode, subtotal);
        if (coupon.isPresent()) {
            session.setAttribute("appliedCouponCode", coupon.get().getCode());
            redirectAttributes.addFlashAttribute("successMessage", "Coupon " + coupon.get().getCode() + " applied successfully!");
        } else {
            session.removeAttribute("appliedCouponCode");
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid coupon code or minimum order criteria not met.");
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove-coupon")
    public String removeCoupon(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("appliedCouponCode");
        redirectAttributes.addFlashAttribute("successMessage", "Coupon removed.");
        return "redirect:/cart";
    }
}
