package com.svjmart;

import com.svjmart.entity.*;
import com.svjmart.repository.OrderRepository;
import com.svjmart.repository.ProductRepository;
import com.svjmart.repository.WishlistRepository;
import com.svjmart.service.ai.AiRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AiRecommendationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    private AiRecommendationService aiRecommendationService;

    @BeforeEach
    void setUp() {
        aiRecommendationService = new AiRecommendationService(productRepository, orderRepository, wishlistRepository);
    }

    @Test
    void testFrequentlyBoughtTogetherLogic() {
        Category staples = new Category("Staples", "rice-grains-staples", "Grains", "icon", 1, true);
        Category oils = new Category("Oils", "cooking-oils-ingredients", "Oils", "icon", 2, true);

        Product atta = new Product();
        atta.setId(1L);
        atta.setName("Aashirvaad Atta");
        atta.setBrand("Aashirvaad");
        atta.setCategory(staples);
        atta.setStockQuantity(50);
        atta.setAvailable(true);

        Product ghee = new Product();
        ghee.setId(2L);
        ghee.setName("Amul Cow Ghee");
        ghee.setBrand("Amul");
        ghee.setCategory(oils);
        ghee.setStockQuantity(20);
        ghee.setAvailable(true);

        when(productRepository.findAll()).thenReturn(List.of(atta, ghee));

        List<Product> recommendations = aiRecommendationService.getFrequentlyBoughtTogether(atta, 5);

        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        // Target product itself must not be in recommendation list
        assertFalse(recommendations.stream().anyMatch(p -> p.getId().equals(atta.getId())));
    }
}
