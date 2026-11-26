package com.example.partner;

import java.math.BigDecimal;

public record PartnerBook(String isbn,
                          String title,
                          double ratingAverage,
                          int reviewsCount,
                          BigDecimal priceUah,
                          boolean inStock,
                          int discountPercent) {
}