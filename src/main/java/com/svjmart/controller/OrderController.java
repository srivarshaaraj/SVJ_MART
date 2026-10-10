package com.svjmart.controller;

import com.svjmart.entity.Order;
import com.svjmart.entity.User;
import com.svjmart.service.OrderService;
import com.svjmart.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }

    @GetMapping
    public String listUserOrders(Authentication authentication, Model model) {
        User user = getAuthenticatedUser(authentication);
        List<Order> orders = orderService.getOrdersForUser(user);
        model.addAttribute("orders", orders);
        return "buyer/orders";
    }

    @GetMapping("/{orderNumber}")
    public String trackOrder(@PathVariable("orderNumber") String orderNumber,
                             Authentication authentication,
                             Model model) {
        User user = getAuthenticatedUser(authentication);
        Order order = orderService.getOrderByNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

        // Ensure user can only view their own order (unless admin)
        if (!order.getUser().getId().equals(user.getId()) && user.getRole() != com.svjmart.entity.Role.ROLE_ADMIN) {
            throw new SecurityException("You do not have permission to view this order.");
        }

        model.addAttribute("order", order);
        return "buyer/order-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(authentication);
        try {
            boolean success = orderService.cancelOrder(id, user);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Order was cancelled successfully and items restored to inventory.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Order cannot be cancelled as it has already progressed past confirmation.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        Order order = orderService.getOrderById(id).orElse(null);
        if (order != null) {
            return "redirect:/orders/" + order.getOrderNumber();
        }
        return "redirect:/orders";
    }
}
