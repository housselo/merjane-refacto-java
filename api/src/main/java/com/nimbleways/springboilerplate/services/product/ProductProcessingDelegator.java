package com.nimbleways.springboilerplate.services.product;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.services.product.ProductProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductProcessingDelegator {

    private final List<ProductProcessor> processors;

    public void process(Product product) {
        processors.stream()
                .filter(p -> p.supports(product))
                .findFirst()
                .ifPresentOrElse(
                        processor -> {
                            log.debug("Delegating processing of product [{}] to [{}]", product.getName(), processor.getClass().getSimpleName());
                            processor.process(product);
                        },
                        () -> {
                            log.warn("No processor found for product type [{}]", product.getType());
                        }
                );
    }
}
