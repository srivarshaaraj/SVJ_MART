package com.svjmart.repository;

import com.svjmart.entity.Order;
import com.svjmart.entity.OrderStatus;
import com.svjmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findTop10ByOrderByOrderDateDesc();

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.finalTotal), 0) FROM Order o WHERE o.status <> 'CANCELLED'")
    BigDecimal calculateTotalGrossRevenue();

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items item WHERE item.seller = :seller ORDER BY o.orderDate DESC")
    List<Order> findOrdersContainingSellerItems(@Param("seller") User seller);
}
