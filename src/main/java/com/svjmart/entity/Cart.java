package com.svjmart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Cart() {
    }

    public Cart(User user) {
        this.user = user;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal getSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getItemTotal());
        }
        return total;
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public CartItem findItemByProduct(Long productId) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    public void addItem(Product product, int quantity) {
        CartItem existing = findItemByProduct(product.getId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUnitPrice(product.getPrice());
        } else {
            CartItem newItem = new CartItem(this, product, quantity, product.getPrice());
            items.add(newItem);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateQuantity(Long productId, int quantity) {
        CartItem item = findItemByProduct(productId);
        if (item != null) {
            if (quantity <= 0) {
                items.remove(item);
                item.setCart(null);
            } else {
                item.setQuantity(quantity);
            }
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeItem(Long productId) {
        CartItem item = findItemByProduct(productId);
        if (item != null) {
            items.remove(item);
            item.setCart(null);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }
}
