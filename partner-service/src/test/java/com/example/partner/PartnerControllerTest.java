package com.example.partner;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PartnerControllerTest {

    private static final String KOBZAR = "978-966-01-0001-1";
    private static final String UNKNOWN = "978-0-00-000000-0";

    private static MockMvc partners(boolean available) {
        PartnerCatalog catalog = new PartnerCatalog();
        return MockMvcBuilders.standaloneSetup(new PartnerController(
                        new ReviewsService(catalog, Duration.ZERO, available),
                        new BookstoreService(catalog, Duration.ZERO, available)))
                .setControllerAdvice(new PartnerExceptionHandler())
                .build();
    }

    @Test
    void knownIsbn_ratingOfferAndDiscount_return200() throws Exception {
        MockMvc mockMvc = partners(true);

        mockMvc.perform(get("/api/partner/books/{isbn}/rating", KOBZAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average").value(4.8))
                .andExpect(jsonPath("$.reviewsCount").value(312));
        mockMvc.perform(get("/api/partner/books/{isbn}/offer", KOBZAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceUah").value(420.00))
                .andExpect(jsonPath("$.inStock").value(true));
        mockMvc.perform(get("/api/partner/books/{isbn}/discount", KOBZAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percent").value(10));
    }

    @Test
    void unknownIsbn_returns404WithMessage() throws Exception {
        partners(true).perform(get("/api/partner/books/{isbn}/rating", UNKNOWN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void partnerUnavailable_returns503() throws Exception {
        partners(false).perform(get("/api/partner/books/{isbn}/offer", KOBZAR))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}