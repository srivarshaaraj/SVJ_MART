package com.svjmart;

import com.svjmart.entity.Coupon;
import com.svjmart.repository.CouponRepository;
import com.svjmart.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(couponRepository);
    }

    @Test
    void testFlatDiscountCalculation() {
        Coupon flatCoupon = new Coupon("FLAT50", 0, new BigDecimal("50.00"), new BigDecimal("200.00"), new BigDecimal("50.00"), true);
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("FLAT50")).thenReturn(Optional.of(flatCoupon));

        BigDecimal discount = couponService.calculateDiscount("FLAT50", new BigDecimal("350.00"));
        assertEquals(new BigDecimal("50.00"), discount);
    }

    @Test
    void testPercentageDiscountWithMaxCap() {
        // 20% off, min order 500, max discount 150
        Coupon pctCoupon = new Coupon("SUPER20", 20, BigDecimal.ZERO, new BigDecimal("500.00"), new BigDecimal("150.00"), true);
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("SUPER20")).thenReturn(Optional.of(pctCoupon));

        // 20% of 1000 is 200, but cap is 150
        BigDecimal discount = couponService.calculateDiscount("SUPER20", new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("150.00"), discount);
    }

    @Test
    void testOrderValueBelowMinimumReceivesZeroDiscount() {
        Coupon minCoupon = new Coupon("MIN500", 10, BigDecimal.ZERO, new BigDecimal("500.00"), new BigDecimal("100.00"), true);
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("MIN500")).thenReturn(Optional.of(minCoupon));

        BigDecimal discount = couponService.calculateDiscount("MIN500", new BigDecimal("300.00"));
        assertEquals(BigDecimal.ZERO, discount);
    }
}
