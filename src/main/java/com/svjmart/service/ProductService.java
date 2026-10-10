package com.svjmart.service;

import com.svjmart.dto.ProductDto;
import com.svjmart.dto.ProductSummaryDto;
import com.svjmart.entity.Category;
import com.svjmart.entity.Product;
import com.svjmart.entity.Role;
import com.svjmart.entity.User;
import com.svjmart.repository.CategoryRepository;
import com.svjmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    public Page<Product> searchProducts(String keyword, Long categoryId, String brand,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        Boolean vegOnly, Boolean inStockOnly,
                                        String sortBy, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price-asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price-desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "averageRating");
        } else if ("popular".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "reviewCount");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.searchAndFilter(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                categoryId,
                (brand != null && !brand.isBlank()) ? brand.trim() : null,
                minPrice,
                maxPrice,
                vegOnly,
                inStockOnly,
                pageable
        );
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsAvailableTrue();
    }

    public List<Product> getBestsellerProducts() {
        return productRepository.findByIsBestsellerTrueAndIsAvailableTrue();
    }

    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategoryAndIsAvailableTrue(category);
    }

    public List<Product> getRelatedProducts(Product product) {
        return productRepository.findTop8ByCategoryAndIdNotAndIsAvailableTrue(product.getCategory(), product.getId());
    }

    public List<Product> getSellerProducts(User seller) {
        return productRepository.findBySeller(seller);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThanEqual(threshold);
    }

    public List<Product> getSellerLowStockProducts(User seller, int threshold) {
        return productRepository.findBySellerAndStockQuantityLessThanEqual(seller, threshold);
    }

    public List<String> getAllBrands() {
        return productRepository.findDistinctBrands();
    }

    public long countTotalProducts() {
        return productRepository.count();
    }

    public long countSellerProducts(User seller) {
        return productRepository.countBySeller(seller);
    }

    @Transactional
    public Product saveProductFromDto(ProductDto dto, User seller) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category selected"));

        Product product;
        if (dto.getId() != null) {
            product = productRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            // Check permissions: seller can only edit own products; admin can edit any
            if (seller.getRole() == Role.ROLE_SELLER && !product.getSeller().getId().equals(seller.getId())) {
                throw new SecurityException("You do not have permission to modify this product.");
            }
        } else {
            product = new Product();
            product.setSeller(seller);
        }

        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setCategory(category);
        product.setSubcategory(dto.getSubcategory());
        product.setDescription(dto.getDescription());
        product.setPackageSize(dto.getPackageSize());
        product.setUnit(dto.getUnit());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice() != null ? dto.getOriginalPrice() : dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setAvailable(dto.getStockQuantity() > 0);
        product.setImageUrl(dto.getImageUrl() != null && !dto.getImageUrl().isBlank() ? dto.getImageUrl() : "/images/placeholder-product.svg");
        product.setPerishable(dto.isPerishable());
        product.setVegetarian(dto.isVegetarian());
        product.setShelfLife(dto.getShelfLife());
        product.setStorageInstructions(dto.getStorageInstructions());
        product.setIngredients(dto.getIngredients());
        product.setNutritionalInfo(dto.getNutritionalInfo());
        product.setFeatured(dto.isFeatured());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id, User actor) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (actor.getRole() == Role.ROLE_SELLER && (product.getSeller() == null || !product.getSeller().getId().equals(actor.getId()))) {
            throw new SecurityException("You cannot delete products belonging to another seller.");
        }

        productRepository.delete(product);
    }

    public List<ProductSummaryDto> getQuickSuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        Pageable limit = PageRequest.of(0, 6);
        return productRepository.searchQuickSuggestions(query.trim(), limit).stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    public ProductSummaryDto mapToSummaryDto(Product p) {
        return new ProductSummaryDto(
                p.getId(),
                p.getName(),
                p.getBrand(),
                p.getCategory() != null ? p.getCategory().getName() : "",
                p.getPackageSize(),
                p.getPrice(),
                p.getOriginalPrice(),
                p.getDiscountPercent(),
                p.getImageUrl(),
                p.getAverageRating(),
                p.isInStock()
        );
    }
}
