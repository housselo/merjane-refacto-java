package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NormalProductProcessor implements ProductProcessor {

    private final ProductRepository repo;
    private final ProductService service;

    @Override
    public boolean supports(Product product) {
        return ProductType.NORMAL.equals(product.getType());
    }

    @Override
    public void process(Product product) {
        if (product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            repo.save(product);
            log.info("Decremented stock for normal product: {}", product.getName());
        } else if (product.getLeadTime() != null && product.getLeadTime() > 0) {
            service.notifyDelay(product.getLeadTime(), product);
            log.info("Notified delay for out-of-stock normal product: {}", product.getName());
        } else {
            log.warn("No stock and no lead time for product: {}", product.getName());
        }
    }
}
