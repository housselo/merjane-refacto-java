package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.product.ExpirableProductProcessor;
import com.nimbleways.springboilerplate.services.product.NormalProductProcessor;
import com.nimbleways.springboilerplate.services.product.NotificationService;
import com.nimbleways.springboilerplate.services.product.ProductService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class ProductProcessingUnitTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private NormalProductProcessor normalProductProcessor;
    private ExpirableProductProcessor expirableProductProcessor;

    @BeforeEach
    void setUp() {
        normalProductProcessor = new NormalProductProcessor(productRepository, productService);
        expirableProductProcessor = new ExpirableProductProcessor(productRepository, notificationService);
    }

    @Test
    void shouldNotifyDelayAndSaveProduct() {
        Product product = createProduct(ProductType.NORMAL, 15, 10, "RJ45 Cable");

        when(productRepository.save(product)).thenReturn(product);

        productService.notifyDelay(product.getLeadTime(), product);

        assertEquals(15, product.getLeadTime());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "RJ45 Cable");
    }

    @Test
    void shouldDecrementNormalProductIfAvailable() {
        Product product = createProduct(ProductType.NORMAL, 5, 10, "Test Product");

        normalProductProcessor.process(product);

        assertEquals(9, product.getAvailable());
        verify(productRepository).save(product);
    }

    @Test
    void shouldNotifyIfProductIsExpired() {
        LocalDate expiredDate = LocalDate.now().minusDays(1);
        Product expiredProduct = createExpirableProduct("Yogurt", 5, 1, expiredDate);

        expirableProductProcessor.process(expiredProduct);

        assertEquals(0, expiredProduct.getAvailable());
        verify(notificationService).sendExpirationNotification("Yogurt", expiredDate);
        verify(productRepository).save(expiredProduct);
    }

    @Test
    void shouldDecrementIfExpirableProductNotExpired() {
        LocalDate expiration = LocalDate.now().plusDays(1);
        Product freshProduct = createExpirableProduct("Cheese", 5, 3, expiration);

        expirableProductProcessor.process(freshProduct);

        assertEquals(2, freshProduct.getAvailable());
        verify(productRepository).save(freshProduct);
        verifyNoInteractions(notificationService);
    }

    // --- Helpers ---

    private Product createProduct(ProductType type, int leadTime, int available, String name) {
        return new Product(null, leadTime, available, type, name, null, null, null);
    }

    private Product createExpirableProduct(String name, int leadTime, int available, LocalDate expirationDate) {
        return new Product(null, leadTime, available, ProductType.EXPIRABLE, name, expirationDate, null, null);
    }
}