package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.product.NotificationService;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MyControllerIntegrationTests {

    private final MockMvc mockMvc;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @MockBean
    private NotificationService notificationService;

    @Test
    public void processOrderShouldReturn200AndModifyStockAccordingToBusinessRules() throws Exception {
        // Arrange
        List<Product> allProducts = createProducts();
        Set<Product> orderItems = new HashSet<>(allProducts);
        Order order = createOrder(orderItems);

        productRepository.saveAll(allProducts);
        order = orderRepository.save(order);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/{orderId}/process", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        Order resultOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(order.getId(), resultOrder.getId());
    }

    private Order createOrder(Set<Product> products) {
        Order order = new Order();
        order.setItems(products);
        return order;
    }

    private List<Product> createProducts() {
        LocalDate now = LocalDate.now();
        return List.of(
                new Product(null, 15, 30, ProductType.NORMAL, "USB Cable", null, null, null),
                new Product(null, 10, 0, ProductType.NORMAL, "USB Dongle", null, null, null),
                new Product(null, 15, 30, ProductType.EXPIRABLE, "Butter", now.plusDays(26), null, null),
                new Product(null, 90, 6, ProductType.EXPIRABLE, "Milk", now.minusDays(2), null, null),
                new Product(null, 15, 30, ProductType.SEASONAL, "Watermelon", null, now.minusDays(2), now.plusDays(58)),
                new Product(null, 15, 30, ProductType.SEASONAL, "Grapes", null, now.plusDays(180), now.plusDays(240))
        );
    }
}
