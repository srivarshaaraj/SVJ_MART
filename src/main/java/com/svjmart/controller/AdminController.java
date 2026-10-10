package com.svjmart.controller;

import com.svjmart.entity.*;
import com.svjmart.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final SellerService sellerService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final CouponService couponService;

    public AdminController(UserService userService,
                           SellerService sellerService,
                           ProductService productService,
                           CategoryService categoryService,
                           OrderService orderService,
                           ReviewService reviewService,
                           CouponService couponService) {
        this.userService = userService;
        this.sellerService = sellerService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.reviewService = reviewService;
        this.couponService = couponService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        BigDecimal grossRevenue = orderService.getTotalGrossRevenue();
        long totalOrders = orderService.countTotalOrders();
        long totalProducts = productService.countTotalProducts();
        long totalBuyers = userService.countByRole(Role.ROLE_BUYER);
        long pendingSellers = sellerService.countPendingSellers();
        long lowStockCount = productService.getLowStockProducts(15).size();
        long pendingReviews = reviewService.countPendingReviews();

        List<Order> recentOrders = orderService.getRecentOrders();

        model.addAttribute("grossRevenue", grossRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalBuyers", totalBuyers);
        model.addAttribute("pendingSellers", pendingSellers);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("pendingReviews", pendingReviews);
        model.addAttribute("recentOrders", recentOrders);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account status updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/sellers")
    public String listSellers(Model model) {
        model.addAttribute("sellers", sellerService.getAllSellers());
        model.addAttribute("pendingCount", sellerService.countPendingSellers());
        return "admin/sellers";
    }

    @PostMapping("/sellers/approve/{id}")
    public String approveSeller(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            sellerService.approveSeller(id);
            redirectAttributes.addFlashAttribute("successMessage", "Seller approved successfully! They can now manage their store.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/suspend/{id}")
    public String suspendSeller(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            sellerService.rejectOrSuspendSeller(id);
            redirectAttributes.addFlashAttribute("successMessage", "Seller suspended from marketplace.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/sellers";
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("newCategory", new Category());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("newCategory") Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/toggle/{id}")
    public String toggleCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.toggleCategoryStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category visibility updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/orders";
    }

    @PostMapping("/orders/status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") OrderStatus newStatus,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, newStatus, null);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + newStatus.getDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/reviews")
    public String listReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "admin/reviews";
    }

    @PostMapping("/reviews/approve/{id}")
    public String approveReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.approveReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review approved for display.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @GetMapping("/coupons")
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponService.getAllCoupons());
        model.addAttribute("newCoupon", new Coupon());
        return "admin/coupons";
    }

    @PostMapping("/coupons/save")
    public String saveCoupon(@ModelAttribute("newCoupon") Coupon coupon, RedirectAttributes redirectAttributes) {
        try {
            couponService.saveCoupon(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/coupons/toggle/{id}")
    public String toggleCoupon(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            couponService.toggleCouponStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon active status updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }
}
