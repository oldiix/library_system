package com.example.partner;

import com.example.common.dto.BookRating;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Slf4j
@Service
public class ReviewsService {

    private final Duration delay;
    private final boolean available;

    public ReviewsService(@Value("${partner.reviews.delay:1s}") Duration delay,
                          @Value("${partner.reviews.available:true}") boolean available) {
        this.delay = delay;
        this.available = available;
    }

    public BookRating ratingFor(String title) {
        SimulatedLatency.pause(delay);
        if (!available) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Сервіс відгуків тимчасово недоступний");
        }
        int seed = title.hashCode();
        double average = 3.5 + Math.floorMod(seed, 15) / 10.0;
        int reviewsCount = 10 + Math.floorMod(seed, 490);
        log.info("Рейтинг для «{}»: {} ({} відгуків)", title, average, reviewsCount);
        return new BookRating(average, reviewsCount, true);
    }
}