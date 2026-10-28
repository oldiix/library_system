package com.example.library_system.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Component
public class FakeBookstoreGateway implements BookstoreGateway {

    private final Duration delay;
    private final boolean available;

    public FakeBookstoreGateway(@Value("${library.external.bookstore.delay:1s}") Duration delay,
                                @Value("${library.external.bookstore.available:true}") boolean available) {
        this.delay = delay;
        this.available = available;
    }

    @Override
    public StoreOffer fetchOffer(String title) {
        SimulatedLatency.pause(delay);
        if (!available) {
            throw new IllegalStateException("Книгарня-партнер недоступна");
        }
        int seed = title.hashCode();
        BigDecimal price = BigDecimal.valueOf(150 + Math.floorMod(seed, 400));
        boolean inStock = Math.floorMod(seed, 3) != 0;
        log.info("Книгарня: «{}» → {} грн, в наявності: {}", title, price, inStock);
        return new StoreOffer(price, inStock, true);
    }
}