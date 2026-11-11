package com.example.common.dto;

import java.math.BigDecimal;

/**
 * Пропозиція книгарні-партнера.
 *
 * @param priceUah  ціна в гривнях (null, якщо дані недоступні)
 * @param inStock   чи є в наявності (null, якщо дані недоступні)
 * @param available чи вдалося отримати дані
 */
public record StoreOffer(BigDecimal priceUah, Boolean inStock, boolean available) {
    public static StoreOffer unavailable() {
        return new StoreOffer(null, null, false);
    }
}