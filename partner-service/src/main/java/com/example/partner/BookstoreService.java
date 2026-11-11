package com.example.partner;

import com.example.common.dto.StoreOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Service
public class BookstoreService {

    private final Duration delay;
    private final boolean available;

    public BookstoreService(@Value("${partner.bookstore.delay:1s}") Duration delay,
                            @Value("${partner.bookstore.available:true}") boolean available) {
        this.delay = delay;
        this.available = available;
    }

    public StoreOffer offerFor(String title) {
        SimulatedLatency.pause(delay);
        if (!available) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Книгарня-партнер тимчасово недоступна");
        }
        int seed = title.hashCode();
        BigDecimal price = BigDecimal.valueOf(150 + Math.floorMod(seed, 400));
        boolean inStock = Math.floorMod(seed, 3) != 0;
        log.info("Пропозиція для «{}»: {} грн, в наявності: {}", title, price, inStock);
        return new StoreOffer(price, inStock, true);
    }
}