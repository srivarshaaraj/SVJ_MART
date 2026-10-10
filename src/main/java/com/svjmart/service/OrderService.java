package com.svjmart.service;

import com.svjmart.dto.CheckoutRequestDto;
import com.svjmart.entity.*;
import com.svjmart.repository.CartRepository;
import com.svjmart.repository.OrderItemRepository;
import com.svjmart.repository.OrderRepository;
import com.svjmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CouponService couponService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        CouponService couponService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.couponService = couponService;
    }

    @Transactional
    public Order placeOrder(User user, CheckoutRequestDto dto) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Your cart is empty."));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        // 1. Concurrently validate inventory & perishable status
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + item.getProduct().getName()));

            if (!product.isAvailable() || product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Item '" + product.getName() + "' is out of stock or insufficient quantity (Available: " + product.getStockQuantity() + ")");
            }
        }

        // 2. Calculate Server-Side Subtotal
        BigDecimal subtotal = cart.getSubtotal();

        // 3. Server-Side Discount Calculation
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (dto.getCouponCode() != null && !dto.getCouponCode().isBlank()) {
            discountAmount = couponService.calculateDiscount(dto.getCouponCode().trim(), subtotal);
        }

        // 4. Delivery Charges: Free delivery above ₹499, otherwise ₹40 flat
        BigDecimal deliveryFee = subtotal.compareTo(new BigDecimal("499.00")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("40.00");

        // 5. Final Payable
        BigDecimal finalTotal = subtotal.subtract(discountAmount).add(deliveryFee);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        // 6. Build Order Entity
        Order order = new Order();
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        order.setOrderNumber("SVJ-" + datePart + "-" + uniqueSuffix);

        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PLACED);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setDeliveryFee(deliveryFee);
        order.setFinalTotal(finalTotal);

        order.setRecipientName(dto.getRecipientName());
        order.setRecipientPhone(dto.getRecipientPhone());
        order.setShippingAddress(dto.getAddressLine1() + (dto.getAddressLine2() != null && !dto.getAddressLine2().isBlank() ? ", " + dto.getAddressLine2() : ""));
        order.setCity(dto.getCity());
        order.setState(dto.getState());
        order.setPincode(dto.getPincode());
        order.setDeliveryInstructions(dto.getDeliveryInstructions());
        order.setPaymentMethod(dto.getPaymentMethod());

        // Simulated payment resolution
        if (dto.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setTransactionReference("COD-" + System.currentTimeMillis());
        } else {
            if (dto.isSimulateSuccess()) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setTransactionReference("SIM-TXN-" + System.currentTimeMillis());
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setTransactionReference("SIM-FAIL-" + System.currentTimeMillis());
            }
        }

        // 7. Deduct inventory and attach OrderItems
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId()).get();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            if (product.getStockQuantity() <= 0) {
                product.setAvailable(false);
            }
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    product.getName(),
                    product.getBrand(),
                    product.getPackageSize(),
                    product.getPrice(),
                    item.getQuantity(),
                    product.getSeller()
            );
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // 8. Empty user's cart
        cart.clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByOrderDateDesc();
    }

    public List<Order> getOrdersForSeller(User seller) {
        return orderRepository.findOrdersContainingSellerItems(seller);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, User actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Status transition validation
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }
        orderRepository.save(order);
    }

    @Transactional
    public boolean cancelOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new SecurityException("Unauthorized to cancel this order");
        }

        if (!order.canBeCancelled()) {
            return false;
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product p = productRepository.findById(item.getProduct().getId()).orElse(null);
            if (p != null) {
                p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                p.setAvailable(true);
                productRepository.save(p);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        orderRepository.save(order);
        return true;
    }

    public BigDecimal getTotalGrossRevenue() {
        return orderRepository.calculateTotalGrossRevenue();
    }

    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public long countTotalOrders() {
        return orderRepository.count();
    }
}
