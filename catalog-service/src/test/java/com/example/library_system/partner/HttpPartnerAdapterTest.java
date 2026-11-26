package com.example.library_system.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import com.example.library_system.config.HttpClientsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpPartnerAdapterTest {

    private static final String KOBZAR = "978-966-01-0001-1";
    private static final String BASE = "http://partner.test/api/partner/books/" + KOBZAR;

    private MockRestServiceServer partner;
    private HttpPartnerAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://partner.test");
        partner = MockRestServiceServer.bindTo(builder).build();
        adapter = new HttpPartnerAdapter(HttpClientsConfig.createPartnerApi(builder.build()));
    }

    @Test
    void declarativeClient_sendsGetToPartnerPaths() {
        partner.expect(requestTo(BASE + "/rating"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess("{\"average\":4.8,\"reviewsCount\":312,\"available\":true}",
                        MediaType.APPLICATION_JSON));
        partner.expect(requestTo(BASE + "/offer")).andRespond(withSuccess(
                "{\"priceUah\":420.00,\"inStock\":true,\"available\":true}", MediaType.APPLICATION_JSON));
        partner.expect(requestTo(BASE + "/discount")).andRespond(withSuccess(
                "{\"isbn\":\"" + KOBZAR + "\",\"percent\":10}", MediaType.APPLICATION_JSON));

        BookRating rating = adapter.findRating(KOBZAR);
        StoreOffer offer = adapter.findOffer(KOBZAR);
        BookDiscount discount = adapter.findDiscount(KOBZAR);

        assertThat(rating.reviewsCount()).isEqualTo(312);
        assertThat(offer.priceUah()).isEqualByComparingTo("420.00");
        assertThat(discount.percent()).isEqualTo(10);
        partner.verify();
    }

    @Test
    void partnerReturns404_becomesNotFound() {
        partner.expect(requestTo(BASE + "/offer")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        PartnerDataNotFoundException exception = assertThrows(PartnerDataNotFoundException.class,
                () -> adapter.findOffer(KOBZAR));

        assertThat(exception.getMessage()).contains(KOBZAR);
    }

    @Test
    void partnerReturns5xx_becomesUnavailable() {
        partner.expect(requestTo(BASE + "/rating")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(PartnerUnavailableException.class, () -> adapter.findRating(KOBZAR));
    }

    @Test
    void partnerReturnsOther4xx_becomesUnavailable() {
        partner.expect(requestTo(BASE + "/discount")).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(PartnerUnavailableException.class, () -> adapter.findDiscount(KOBZAR));
    }

    @Test
    void connectionRefused_becomesUnavailableAndHidesClientException() {
        partner.expect(requestTo(BASE + "/rating")).andRespond(request -> {
            throw new ConnectException("Connection refused");
        });

        PartnerUnavailableException exception = assertThrows(PartnerUnavailableException.class,
                () -> adapter.findRating(KOBZAR));

        assertThat(exception).isNotInstanceOf(RestClientException.class);
        assertThat(exception.getCause()).isInstanceOf(RestClientException.class);
    }

    @Test
    void silentPartner_readTimeoutStopsWaitingAndBecomesUnavailable() throws Exception {
        try (ServerSocket silentPartner = new ServerSocket(0)) {
            RestClient restClient = RestClient.builder()
                    .baseUrl("http://localhost:" + silentPartner.getLocalPort())
                    .requestFactory(HttpClientsConfig.requestFactory(Duration.ofSeconds(1), Duration.ofMillis(300)))
                    .build();
            HttpPartnerAdapter timedAdapter = new HttpPartnerAdapter(HttpClientsConfig.createPartnerApi(restClient));

            long started = System.nanoTime();
            PartnerUnavailableException exception = assertThrows(PartnerUnavailableException.class,
                    () -> timedAdapter.findRating(KOBZAR));
            Duration waited = Duration.ofNanos(System.nanoTime() - started);

            assertThat(waited).isLessThan(Duration.ofSeconds(2));
            assertThat(exception).hasRootCauseInstanceOf(SocketTimeoutException.class);
        }
    }
}