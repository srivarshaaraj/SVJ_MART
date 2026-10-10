package com.svjmart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "discount_percent", nullable = false)
    private int discountPercent = 0;

    @Column(name = "flat_discount", precision = 10, scale = 2)
    private BigDecimal flatDiscount = BigDecimal.ZERO;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(nullable = false)
    private boolean active = true;

    public Coupon() {
    }

    public Coupon(String code, int discountPercent, BigDecimal flatDiscount, BigDecimal minOrderValue, BigDecimal maxDiscount, boolean active) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.flatDiscount = flatDiscount != null ? flatDiscount : BigDecimal.ZERO;
        this.minOrderValue = minOrderValue != null ? minOrderValue : BigDecimal.ZERO;
        this.maxDiscount = maxDiscount;
        this.active = active;
    }

    public BigDecimal calculateDiscount(BigDecimal orderSubtotal) {
        if (!active || orderSubtotal == null || orderSubtotal.compareTo(minOrderValue) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (discountPercent > 0) {
            discount = orderSubtotal.multiply(BigDecimal.valueOf(discountPercent))
                                   .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (flatDiscount != null && flatDiscount.compareTo(BigDecimal.ZERO) > 0) {
            discount = flatDiscount;
        }

        if (maxDiscount != null && maxDiscount.compareTo(BigDecimal.ZERO) > 0 && discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }

        if (discount.compareTo(orderSubtotal) > 0) {
            discount = orderSubtotal;
        }

        return discount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getFlatDiscount() {
        return flatDiscount;
    }

    public void setFlatDiscount(BigDecimal flatDiscount) {
        this.flatDiscount = flatDiscount;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
