package com.svjmart;

import com.svjmart.entity.*;
import com.svjmart.repository.CartRepository;
import com.svjmart.repository.ProductRepository;
import com.svjmart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    private CartService cartService;
    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, productRepository);

        testUser = new User("Test Buyer", "test@buyer.com", "secret", "9876543210", Role.ROLE_BUYER);
        testUser.setId(10L);

        testProduct = new Product();
        testProduct.setId(100L);
        testProduct.setName("Aashirvaad Atta");
        testProduct.setPrice(new BigDecimal("285.00"));
        testProduct.setStockQuantity(5);
        testProduct.setAvailable(true);

        testCart = new Cart(testUser);
        testCart.setId(50L);
    }

    @Test
    void testAddToCartSuccess() {
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));

        cartService.addToCart(testUser, 100L, 2);

        assertEquals(1, testCart.getItems().size());
        assertEquals(2, testCart.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("570.00"), testCart.getSubtotal());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void testAddToCartThrowsWhenStockExceeded() {
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));

        // Available is 5, attempting to add 10
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cartService.addToCart(testUser, 100L, 10);
        });

        assertTrue(exception.getMessage().contains("Only 5 units"));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void testAddToCartThrowsWhenOutOfStock() {
        testProduct.setStockQuantity(0);
        testProduct.setAvailable(false);

        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));

        assertThrows(IllegalStateException.class, () -> {
            cartService.addToCart(testUser, 100L, 1);
        });
    }
}
