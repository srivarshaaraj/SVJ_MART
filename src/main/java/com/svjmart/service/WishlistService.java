package com.svjmart.service;

import com.svjmart.entity.Product;
import com.svjmart.entity.User;
import com.svjmart.entity.Wishlist;
import com.svjmart.repository.ProductRepository;
import com.svjmart.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUser(user)
                .orElseGet(() -> wishlistRepository.save(new Wishlist(user)));
    }

    @Transactional
    public boolean toggleWishlist(User user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        boolean added;
        if (wishlist.containsProduct(product)) {
            wishlist.removeProduct(product);
            added = false;
        } else {
            wishlist.addProduct(product);
            added = true;
        }

        wishlistRepository.save(wishlist);
        return added;
    }

    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        wishlist.removeProduct(product);
        wishlistRepository.save(wishlist);
    }

    public int getWishlistCount(User user) {
        if (user == null) {
            return 0;
        }
        return wishlistRepository.findByUser(user)
                .map(w -> w.getProducts().size())
                .orElse(0);
    }

    public boolean isInWishlist(User user, Long productId) {
        if (user == null) {
            return false;
        }
        return wishlistRepository.findByUser(user)
                .map(w -> w.getProducts().stream().anyMatch(p -> p.getId().equals(productId)))
                .orElse(false);
    }
}
