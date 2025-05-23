package com.nimbleways.springboilerplate.services.product;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.product.ProductProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpirableProductProcessor implements ProductProcessor {

    private final ProductRepository pr;
    private final NotificationService ns;

    @Override
    public boolean supports(Product product) {
        return ProductType.EXPIRABLE.equals(product.getType());
    }

    @Override
    public void process(Product product) {
        LocalDate now = LocalDate.now();

        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(now)) {
            product.setAvailable(product.getAvailable() - 1);
        } else {
            ns.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
        }

        pr.save(product);
    }
}