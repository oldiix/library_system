package com.example.library_system.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class FakeReviewsGateway implements ReviewsGateway {

    private final Duration delay;
    private final boolean available;

    public FakeReviewsGateway(@Value("${library.external.reviews.delay:1s}") Duration delay,
                              @Value("${library.external.reviews.available:true}") boolean available) {
        this.delay = delay;
        this.available = available;
    }

    @Override
    public BookRating fetchRating(String title) {
        SimulatedLatency.pause(delay);
        if (!available) {
            throw new IllegalStateException("Сервіс відгуків недоступний");
        }
        int seed = title.hashCode();
        double average = 3.5 + Math.floorMod(seed, 15) / 10.0;
        int reviewsCount = 10 + Math.floorMod(seed, 490);
        log.info("Сервіс відгуків: «{}» → {} ({} відгуків)", title, average, reviewsCount);
        return new BookRating(average, reviewsCount, true);
    }
}