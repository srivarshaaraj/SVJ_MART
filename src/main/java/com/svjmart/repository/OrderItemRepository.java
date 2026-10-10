package com.svjmart.repository;

import com.svjmart.entity.OrderItem;
import com.svjmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findBySellerOrderByOrderOrderDateDesc(User seller);
    long countBySeller(User seller);

    @Query("SELECT COALESCE(SUM(oi.subtotalPrice), 0) FROM OrderItem oi WHERE oi.seller = :seller AND oi.order.status <> 'CANCELLED'")
    BigDecimal calculateSellerTotalRevenue(@Param("seller") User seller);
}
