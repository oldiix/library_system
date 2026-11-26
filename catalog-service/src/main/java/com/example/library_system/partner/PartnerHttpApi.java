package com.example.library_system.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/api/partner/books", accept = "application/json")
public interface PartnerHttpApi {

    @GetExchange("/{isbn}/rating")
    BookRating rating(@PathVariable String isbn);

    @GetExchange("/{isbn}/offer")
    StoreOffer offer(@PathVariable String isbn);

    @GetExchange("/{isbn}/discount")
    BookDiscount discount(@PathVariable String isbn);
}