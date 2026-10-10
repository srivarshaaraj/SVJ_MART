package com.svjmart.dto;

import java.math.BigDecimal;

public class ProductSummaryDto {
    private Long id;
    private String name;
    private String brand;
    private String categoryName;
    private String packageSize;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int discountPercent;
    private String imageUrl;
    private double rating;
    private boolean inStock;

    public ProductSummaryDto() {
    }

    public ProductSummaryDto(Long id, String name, String brand, String categoryName, String packageSize,
                             BigDecimal price, BigDecimal originalPrice, int discountPercent, String imageUrl,
                             double rating, boolean inStock) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.categoryName = categoryName;
        this.packageSize = packageSize;
        this.price = price;
        this.originalPrice = originalPrice;
        this.discountPercent = discountPercent;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.inStock = inStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(String packageSize) {
        this.packageSize = packageSize;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
