package com.svjmart.controller;

import com.svjmart.dto.ProductDto;
import com.svjmart.entity.*;
import com.svjmart.repository.OrderItemRepository;
import com.svjmart.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
public class SellerController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final SellerService sellerService;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    public SellerController(ProductService productService,
                            CategoryService categoryService,
                            OrderService orderService,
                            SellerService sellerService,
                            OrderItemRepository orderItemRepository,
                            UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.sellerService = sellerService;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
    }

    private User getAuthenticatedSeller(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated seller not found."));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User seller = getAuthenticatedSeller(authentication);
        SellerProfile profile = sellerService.getProfileByUser(seller).orElse(null);

        boolean isApproved = profile != null && profile.isApproved();
        model.addAttribute("isApproved", isApproved);
        model.addAttribute("sellerProfile", profile);

        List<Product> products = productService.getSellerProducts(seller);
        List<Product> lowStockProducts = productService.getSellerLowStockProducts(seller, 15);
        List<Order> orders = orderService.getOrdersForSeller(seller);
        BigDecimal sellerRevenue = orderItemRepository.calculateSellerTotalRevenue(seller);

        model.addAttribute("productsCount", products.size());
        model.addAttribute("lowStockCount", lowStockProducts.size());
        model.addAttribute("ordersCount", orders.size());
        model.addAttribute("totalRevenue", sellerRevenue);
        model.addAttribute("recentProducts", products.stream().limit(5).toList());
        model.addAttribute("lowStockProducts", lowStockProducts);

        return "seller/dashboard";
    }

    @GetMapping("/products")
    public String listProducts(Authentication authentication, Model model) {
        User seller = getAuthenticatedSeller(authentication);
        List<Product> products = productService.getSellerProducts(seller);
        model.addAttribute("products", products);
        return "seller/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        ProductDto dto = new ProductDto();
        dto.setStockQuantity(20);
        model.addAttribute("productDto", dto);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("isEdit", false);
        return "seller/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable("id") Long id, Authentication authentication, Model model) {
        User seller = getAuthenticatedSeller(authentication);
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (seller.getRole() == Role.ROLE_SELLER && !product.getSeller().getId().equals(seller.getId())) {
            throw new SecurityException("You do not have access to edit this product.");
        }

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setSubcategory(product.getSubcategory());
        dto.setDescription(product.getDescription());
        dto.setPackageSize(product.getPackageSize());
        dto.setUnit(product.getUnit());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setPerishable(product.isPerishable());
        dto.setVegetarian(product.isVegetarian());
        dto.setShelfLife(product.getShelfLife());
        dto.setStorageInstructions(product.getStorageInstructions());
        dto.setIngredients(product.getIngredients());
        dto.setNutritionalInfo(product.getNutritionalInfo());
        dto.setFeatured(product.isFeatured());

        model.addAttribute("productDto", dto);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("isEdit", true);
        return "seller/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute("productDto") ProductDto dto,
                              BindingResult bindingResult,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User seller = getAuthenticatedSeller(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("isEdit", dto.getId() != null);
            return "seller/product-form";
        }

        try {
            productService.saveProductFromDto(dto, seller);
            redirectAttributes.addFlashAttribute("successMessage",
                    dto.getId() != null ? "Product updated successfully!" : "New product published to supermarket catalog!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/seller/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User seller = getAuthenticatedSeller(authentication);
        try {
            productService.deleteProduct(id, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Product removed from catalog.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/products";
    }

    @GetMapping("/orders")
    public String listOrders(Authentication authentication, Model model) {
        User seller = getAuthenticatedSeller(authentication);
        List<Order> orders = orderService.getOrdersForSeller(seller);
        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "seller/orders";
    }

    @PostMapping("/orders/status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") OrderStatus newStatus,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        User seller = getAuthenticatedSeller(authentication);
        try {
            orderService.updateOrderStatus(orderId, newStatus, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + newStatus.getDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/orders";
    }

    @GetMapping("/inventory")
    public String inventoryAlerts(Authentication authentication, Model model) {
        User seller = getAuthenticatedSeller(authentication);
        List<Product> lowStock = productService.getSellerLowStockProducts(seller, 15);
        model.addAttribute("lowStockProducts", lowStock);
        return "seller/inventory";
    }
}
