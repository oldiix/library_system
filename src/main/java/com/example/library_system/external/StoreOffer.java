package com.example.library_system.external;

import java.math.BigDecimal;

public record StoreOffer(BigDecimal priceUah, Boolean inStock, boolean available) {

    public static StoreOffer unavailable() {
        return new StoreOffer(null, null, false);
    }
}