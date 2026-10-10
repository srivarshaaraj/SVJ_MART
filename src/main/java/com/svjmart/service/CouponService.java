package com.svjmart.service;

import com.svjmart.entity.Coupon;
import com.svjmart.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<Coupon> getActiveCoupons() {
        return couponRepository.findByActiveTrue();
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Optional<Coupon> findValidCoupon(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return couponRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim())
                .filter(coupon -> subtotal == null || subtotal.compareTo(coupon.getMinOrderValue()) >= 0);
    }

    public BigDecimal calculateDiscount(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank() || subtotal == null) {
            return BigDecimal.ZERO;
        }
        return couponRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim())
                .map(coupon -> coupon.calculateDiscount(subtotal))
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public Coupon saveCoupon(Coupon coupon) {
        coupon.setCode(coupon.getCode().toUpperCase().trim());
        return couponRepository.save(coupon);
    }

    @Transactional
    public void toggleCouponStatus(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
    }
}
