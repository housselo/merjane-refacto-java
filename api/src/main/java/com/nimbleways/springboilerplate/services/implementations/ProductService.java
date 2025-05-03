package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    /**
     * Notifies clients of a delay and persists the updated product state.
     */
    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime); // Only if lead time changes, otherwise remove this line
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    /**
     * Optional: Generic stock decrement helper
     */
    public void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    /**
     * Optional: Mark a product as unavailable and notify
     */
    public void markUnavailableWithNotification(Product product, String reason) {
        product.setAvailable(0);
        productRepository.save(product);
        notificationService.sendOutOfStockNotification(product.getName());
    }
}
