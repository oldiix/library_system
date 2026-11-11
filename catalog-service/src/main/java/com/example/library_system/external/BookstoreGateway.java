package com.example.library_system.external;

import com.example.common.dto.StoreOffer;

public interface BookstoreGateway {

    StoreOffer fetchOffer(String title);
}