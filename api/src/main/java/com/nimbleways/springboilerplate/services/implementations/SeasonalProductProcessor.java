package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonalProductProcessor implements ProductProcessor {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final ProductService productService;

    @Override
    public boolean supports(Product product) {
        return ProductType.SEASONAL.equals(product.getType());
    }

    @Override
    public void process(Product product) {
        LocalDate now = LocalDate.now();

        boolean inSeason = isInSeason(product, now);
        boolean restockAfterSeason = now.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate());

        if (inSeason && product.getAvailable() > 0) {
            decrementStock(product);
        } else if (restockAfterSeason || product.getSeasonStartDate().isAfter(now)) {
            notifyUnavailable(product);
        } else {
            productService.notifyDelay(product.getLeadTime(), product);
            log.info("Notified delay for seasonal product '{}'", product.getName());
        }
    }

    private boolean isInSeason(Product product, LocalDate now) {
        return !now.isBefore(product.getSeasonStartDate()) && !now.isAfter(product.getSeasonEndDate());
    }

    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
        log.info("Stock decremented for seasonal product '{}'", product.getName());
    }

    private void notifyUnavailable(Product product) {
        product.setAvailable(0);
        productRepository.save(product);
        notificationService.sendOutOfStockNotification(product.getName());
        log.info("Product '{}' is out of season or cannot be restocked in time", product.getName());
    }
}