package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@UnitTest
public class MyUnitTests {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private NormalProductProcessor normalProductProcessor;
    private ExpirableProductProcessor expirableProductProcessor;

    @BeforeEach
    public void setup() {
        normalProductProcessor = new NormalProductProcessor(productRepository, productService);
        expirableProductProcessor = new ExpirableProductProcessor(productRepository, notificationService);
    }

    @Test
    public void shouldNotifyDelayAndSaveProduct() {
        // GIVEN
        Product product = new Product(null, 15, 10, ProductType.NORMAL, "RJ45 Cable", null, null, null);
        when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.notifyDelay(product.getLeadTime(), product);

        // THEN
        assertEquals(15, product.getLeadTime());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "RJ45 Cable");
    }

    @Test
    public void shouldDecrementNormalProductIfAvailable() {
        // GIVEN
        Product product = new Product(null, 5, 10, ProductType.NORMAL, "Test Product", null, null, null);

        // WHEN
        normalProductProcessor.process(product);

        // THEN
        assertEquals(9, product.getAvailable());
        verify(productRepository).save(product);
    }

    @Test
    public void shouldNotifyIfProductIsExpired() {
        // GIVEN
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Product expiredProduct = new Product(null, 5, 1, ProductType.EXPIRABLE, "Yogurt", yesterday, null, null);

        // WHEN
        expirableProductProcessor.process(expiredProduct);

        // THEN
        assertEquals(0, expiredProduct.getAvailable());
        verify(notificationService).sendExpirationNotification("Yogurt", yesterday);
        verify(productRepository).save(expiredProduct);
    }

    @Test
    public void shouldDecrementIfExpirableProductNotExpired() {
        // GIVEN
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Product freshProduct = new Product(null, 5, 3, ProductType.EXPIRABLE, "Cheese", tomorrow, null, null);

        // WHEN
        expirableProductProcessor.process(freshProduct);

        // THEN
        assertEquals(2, freshProduct.getAvailable());
        verify(productRepository).save(freshProduct);
        verifyNoInteractions(notificationService);
    }
}
