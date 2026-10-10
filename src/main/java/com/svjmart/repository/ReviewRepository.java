package com.svjmart.repository;

import com.svjmart.entity.Product;
import com.svjmart.entity.Review;
import com.svjmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductAndApprovedTrueOrderByCreatedAtDesc(Product product);
    List<Review> findByUserOrderByCreatedAtDesc(User user);
    List<Review> findAllByOrderByCreatedAtDesc();
    List<Review> findByApprovedFalseOrderByCreatedAtDesc();
    long countByApprovedFalse();
}
