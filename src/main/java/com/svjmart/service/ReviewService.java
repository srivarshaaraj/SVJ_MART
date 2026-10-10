package com.svjmart.service;

import com.svjmart.dto.ReviewDto;
import com.svjmart.entity.Product;
import com.svjmart.entity.Review;
import com.svjmart.entity.User;
import com.svjmart.repository.ProductRepository;
import com.svjmart.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    public List<Review> getReviewsForProduct(Product product) {
        return reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(product);
    }

    public List<Review> getReviewsForUser(User user) {
        return reviewRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Review> getPendingReviews() {
        return reviewRepository.findByApprovedFalseOrderByCreatedAtDesc();
    }

    public long countPendingReviews() {
        return reviewRepository.countByApprovedFalse();
    }

    @Transactional
    public Review addReview(User user, ReviewDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Review review = new Review(
                product,
                user,
                dto.getRating(),
                dto.getHeadline(),
                dto.getComment(),
                true
        );

        Review saved = reviewRepository.save(review);

        // Recalculate product rating
        List<Review> approvedReviews = reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(product);
        if (!approvedReviews.isEmpty()) {
            double avg = approvedReviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
            product.setAverageRating(Math.round(avg * 10.0) / 10.0);
            product.setReviewCount(approvedReviews.size());
            productRepository.save(product);
        }

        return saved;
    }

    @Transactional
    public void approveReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setApproved(true);
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        Product product = review.getProduct();
        reviewRepository.delete(review);

        // Re-compute average
        List<Review> approvedReviews = reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(product);
        if (!approvedReviews.isEmpty()) {
            double avg = approvedReviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
            product.setAverageRating(Math.round(avg * 10.0) / 10.0);
            product.setReviewCount(approvedReviews.size());
        } else {
            product.setReviewCount(0);
        }
        productRepository.save(product);
    }
}
