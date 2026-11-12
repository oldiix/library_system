package com.example.library_system.external;

import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PartnerServiceGatewayTest {

    private MockRestServiceServer server;
    private PartnerServiceGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://partner");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new PartnerServiceGateway(builder.build());
    }

    @Test
    void fetchRating_partnerResponds_returnsRatingFromJson() {

        server.expect(requestTo("http://partner/api/partner/ratings?title=Kobzar"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"average\":4.7,\"reviewsCount\":120,\"available\":true}",
                        MediaType.APPLICATION_JSON));

        BookRating rating = gateway.fetchRating("Kobzar");

        assertThat(rating).isEqualTo(new BookRating(4.7, 120, true));
        server.verify();
    }

    @Test
    void fetchOffer_partnerResponds_returnsOfferFromJson() {

        server.expect(requestTo("http://partner/api/partner/offers?title=Kobzar"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"priceUah\":250,\"inStock\":true,\"available\":true}",
                        MediaType.APPLICATION_JSON));

        StoreOffer offer = gateway.fetchOffer("Kobzar");

        assertThat(offer.available()).isTrue();
        assertThat(offer.inStock()).isTrue();
        assertThat(offer.priceUah()).isEqualByComparingTo("250");
        server.verify();
    }

    @Test
    void fetchRating_partnerUnavailable_throwsException() {

        server.expect(requestTo("http://partner/api/partner/ratings?title=Kobzar"))
                .andRespond(withServiceUnavailable());

        assertThrows(RestClientException.class, () -> gateway.fetchRating("Kobzar"));
    }

    @Test
    void fetchOffer_partnerUnavailable_throwsException() {

        server.expect(requestTo("http://partner/api/partner/offers?title=Kobzar"))
                .andRespond(withServiceUnavailable());

        assertThrows(RestClientException.class, () -> gateway.fetchOffer("Kobzar"));
    }
}
