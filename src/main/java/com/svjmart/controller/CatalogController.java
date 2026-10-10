package com.svjmart.controller;

import com.svjmart.dto.ReviewDto;
import com.svjmart.entity.Category;
import com.svjmart.entity.Product;
import com.svjmart.entity.Review;
import com.svjmart.entity.User;
import com.svjmart.service.CategoryService;
import com.svjmart.service.ProductService;
import com.svjmart.service.ReviewService;
import com.svjmart.service.UserService;
import com.svjmart.service.WishlistService;
import com.svjmart.service.ai.AiRecommendationService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class CatalogController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final AiRecommendationService aiRecommendationService;
    private final WishlistService wishlistService;
    private final UserService userService;

    public CatalogController(ProductService productService,
                             CategoryService categoryService,
                             ReviewService reviewService,
                             AiRecommendationService aiRecommendationService,
                             WishlistService wishlistService,
                             UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.aiRecommendationService = aiRecommendationService;
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    @GetMapping("/catalog")
    public String browseCatalog(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "vegOnly", required = false) Boolean vegOnly,
            @RequestParam(value = "inStockOnly", required = false) Boolean inStockOnly,
            @RequestParam(value = "sort", defaultValue = "featured") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        int pageSize = 12;
        Page<Product> productsPage = productService.searchProducts(
                keyword, categoryId, brand, minPrice, maxPrice, vegOnly, inStockOnly, sort, page, pageSize
        );

        Category selectedCategory = null;
        if (categoryId != null) {
            selectedCategory = categoryService.getCategoryById(categoryId).orElse(null);
        }

        model.addAttribute("productsPage", productsPage);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("vegOnly", vegOnly);
        model.addAttribute("inStockOnly", inStockOnly);
        model.addAttribute("currentSort", sort);
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("categories", categoryService.getActiveCategories());

        return "catalog/list";
    }

    @GetMapping("/catalog/{categorySlug}")
    public String browseByCategory(
            @PathVariable("categorySlug") String categorySlug,
            @RequestParam(value = "sort", defaultValue = "featured") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        Category category = categoryService.getCategoryBySlug(categorySlug)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categorySlug));

        return browseCatalog(null, category.getId(), null, null, null, null, null, sort, page, model);
    }

    @GetMapping("/product/{id}")
    public String viewProductDetails(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        User user = null;
        boolean inWishlist = false;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                inWishlist = wishlistService.isInWishlist(user, product.getId());
            }
        }

        List<Product> relatedProducts = productService.getRelatedProducts(product);
        List<Product> frequentlyBoughtTogether = aiRecommendationService.getFrequentlyBoughtTogether(product, 4);
        List<Review> reviews = reviewService.getReviewsForProduct(product);

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setProductId(product.getId());

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("frequentlyBoughtTogether", frequentlyBoughtTogether);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewDto", reviewDto);
        model.addAttribute("inWishlist", inWishlist);

        return "catalog/detail";
    }
}
