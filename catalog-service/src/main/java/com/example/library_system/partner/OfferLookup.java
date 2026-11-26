package com.example.library_system.partner;

import com.example.common.dto.StoreOffer;

public interface OfferLookup {

    StoreOffer findOffer(String isbn);
}