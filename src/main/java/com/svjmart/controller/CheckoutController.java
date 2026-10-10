package com.svjmart.controller;

import com.svjmart.dto.CheckoutRequestDto;
import com.svjmart.entity.*;
import com.svjmart.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final CouponService couponService;
    private final UserService userService;

    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              AddressService addressService,
                              CouponService couponService,
                              UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.addressService = addressService;
        this.couponService = couponService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }

    @GetMapping
    public String checkoutPage(Authentication authentication, Model model, HttpSession session) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        List<Address> addresses = addressService.getAddressesForUser(user);
        BigDecimal subtotal = cart.getSubtotal();
        String appliedCoupon = (String) session.getAttribute("appliedCouponCode");
        BigDecimal discount = couponService.calculateDiscount(appliedCoupon, subtotal);
        BigDecimal deliveryFee = subtotal.compareTo(new BigDecimal("499.00")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("40.00");
        BigDecimal finalTotal = subtotal.subtract(discount).add(deliveryFee);

        CheckoutRequestDto checkoutDto = new CheckoutRequestDto();
        checkoutDto.setCouponCode(appliedCoupon);

        // Pre-fill with default address if present
        Address defaultAddr = addressService.getDefaultAddress(user).orElse(!addresses.isEmpty() ? addresses.get(0) : null);
        if (defaultAddr != null) {
            checkoutDto.setSelectedAddressId(defaultAddr.getId());
            checkoutDto.setRecipientName(defaultAddr.getFullName());
            checkoutDto.setRecipientPhone(defaultAddr.getPhone());
            checkoutDto.setAddressLine1(defaultAddr.getAddressLine1());
            checkoutDto.setAddressLine2(defaultAddr.getAddressLine2());
            checkoutDto.setCity(defaultAddr.getCity());
            checkoutDto.setState(defaultAddr.getState());
            checkoutDto.setPincode(defaultAddr.getPincode());
            checkoutDto.setLandmark(defaultAddr.getLandmark());
        }

        model.addAttribute("cart", cart);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("checkoutDto", checkoutDto);

        return "buyer/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@Valid @ModelAttribute("checkoutDto") CheckoutRequestDto checkoutDto,
                             BindingResult bindingResult,
                             Authentication authentication,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            List<Address> addresses = addressService.getAddressesForUser(user);
            BigDecimal subtotal = cart.getSubtotal();
            String appliedCoupon = (String) session.getAttribute("appliedCouponCode");
            BigDecimal discount = couponService.calculateDiscount(appliedCoupon, subtotal);
            BigDecimal deliveryFee = subtotal.compareTo(new BigDecimal("499.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("40.00");
            BigDecimal finalTotal = subtotal.subtract(discount).add(deliveryFee);

            model.addAttribute("cart", cart);
            model.addAttribute("addresses", addresses);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("discount", discount);
            model.addAttribute("deliveryFee", deliveryFee);
            model.addAttribute("finalTotal", finalTotal);
            return "buyer/checkout";
        }

        try {
            checkoutDto.setCouponCode((String) session.getAttribute("appliedCouponCode"));
            Order order = orderService.placeOrder(user, checkoutDto);
            session.removeAttribute("appliedCouponCode");

            redirectAttributes.addFlashAttribute("orderSuccess", true);
            return "redirect:/orders/" + order.getOrderNumber();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        }
    }
}
