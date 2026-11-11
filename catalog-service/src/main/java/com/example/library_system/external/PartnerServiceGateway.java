package com.example.library_system.external;

import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import com.example.library_system.config.HttpClientsConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PartnerServiceGateway implements ReviewsGateway, BookstoreGateway {

    private final RestClient restClient;

    public PartnerServiceGateway(@Qualifier(HttpClientsConfig.PARTNER_REST_CLIENT) RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public BookRating fetchRating(String title) {
        return restClient.get()
                .uri(uri -> uri.path("/api/partner/ratings").queryParam("title", title).build())
                .retrieve()
                .body(BookRating.class);
    }

    @Override
    public StoreOffer fetchOffer(String title) {
        return restClient.get()
                .uri(uri -> uri.path("/api/partner/offers").queryParam("title", title).build())
                .retrieve()
                .body(StoreOffer.class);
    }
}