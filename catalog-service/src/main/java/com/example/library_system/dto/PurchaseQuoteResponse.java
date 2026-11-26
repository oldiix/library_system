package com.example.library_system.dto;

import java.math.BigDecimal;

public record PurchaseQuoteResponse(Long bookId,
                                    String title,
                                    String isbn,
                                    BigDecimal partnerPriceUah,
                                    int discountPercent,
                                    BigDecimal priceAfterDiscountUah,
                                    boolean inStock) {
}