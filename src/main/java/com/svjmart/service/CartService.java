package com.svjmart.service;

import com.svjmart.entity.Cart;
import com.svjmart.entity.CartItem;
import com.svjmart.entity.Product;
import com.svjmart.entity.User;
import com.svjmart.repository.CartRepository;
import com.svjmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.isAvailable() || product.getStockQuantity() <= 0) {
            throw new IllegalStateException("Sorry, " + product.getName() + " is currently out of stock.");
        }

        Cart cart = getOrCreateCart(user);
        CartItem existing = cart.findItemByProduct(productId);
        int currentQty = existing != null ? existing.getQuantity() : 0;
        int targetQty = currentQty + quantity;

        if (targetQty > product.getStockQuantity()) {
            throw new IllegalStateException("Only " + product.getStockQuantity() + " units of " + product.getName() + " are available in stock.");
        }

        cart.addItem(product, quantity);
        cartRepository.save(cart);
    }

    @Transactional
    public void updateQuantity(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (quantity > product.getStockQuantity()) {
            throw new IllegalStateException("Only " + product.getStockQuantity() + " units of " + product.getName() + " are available in stock.");
        }

        cart.updateQuantity(productId, quantity);
        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.clear();
        cartRepository.save(cart);
    }

    public int getCartItemCount(User user) {
        if (user == null) {
            return 0;
        }
        return cartRepository.findByUser(user)
                .map(Cart::getTotalItemCount)
                .orElse(0);
    }
}
