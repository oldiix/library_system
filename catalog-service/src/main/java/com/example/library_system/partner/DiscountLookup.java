package com.example.library_system.partner;

import com.example.common.dto.BookDiscount;

public interface DiscountLookup {

    BookDiscount findDiscount(String isbn);
}