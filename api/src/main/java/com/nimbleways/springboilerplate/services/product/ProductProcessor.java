package com.nimbleways.springboilerplate.services.product;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductProcessor {
    boolean supports(Product product);
    void process(Product product);
}