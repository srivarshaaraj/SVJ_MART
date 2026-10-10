package com.svjmart.repository;

import com.svjmart.entity.Category;
import com.svjmart.entity.Product;
import com.svjmart.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    List<Product> findByCategoryAndIsAvailableTrue(Category category);

    Page<Product> findByCategoryAndIsAvailableTrue(Category category, Pageable pageable);

    Page<Product> findByIsAvailableTrue(Pageable pageable);

    List<Product> findByIsFeaturedTrueAndIsAvailableTrue();

    List<Product> findByIsBestsellerTrueAndIsAvailableTrue();

    List<Product> findBySeller(User seller);

    long countBySeller(User seller);

    List<Product> findByStockQuantityLessThanEqual(int threshold);

    List<Product> findBySellerAndStockQuantityLessThanEqual(User seller, int threshold);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL ORDER BY p.brand ASC")
    List<String> findDistinctBrands();

    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:vegOnly IS NULL OR :vegOnly = false OR p.isVegetarian = true) AND " +
           "(:inStockOnly IS NULL OR :inStockOnly = false OR p.stockQuantity > 0) AND " +
           "p.isAvailable = true")
    Page<Product> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("vegOnly") Boolean vegOnly,
            @Param("inStockOnly") Boolean inStockOnly,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchQuickSuggestions(@Param("query") String query, Pageable pageable);

    List<Product> findTop8ByCategoryAndIdNotAndIsAvailableTrue(Category category, Long id);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.price <= :budget AND p.stockQuantity > 0 ORDER BY p.averageRating DESC")
    List<Product> findBudgetRecommendations(@Param("categoryId") Long categoryId, @Param("budget") BigDecimal budget, Pageable pageable);
}
