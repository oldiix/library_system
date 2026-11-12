package com.example.partner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PartnerControllerTest {

    private MockMvc availablePartners;
    private MockMvc unavailablePartners;

    @BeforeEach
    void setUp() {
        availablePartners = MockMvcBuilders.standaloneSetup(new PartnerController(
                new ReviewsService(Duration.ZERO, true),
                new BookstoreService(Duration.ZERO, true))).build();
        unavailablePartners = MockMvcBuilders.standaloneSetup(new PartnerController(
                new ReviewsService(Duration.ZERO, false),
                new BookstoreService(Duration.ZERO, false))).build();
    }

    @Test
    void ratingsAndOffers_partnersAvailable_returnJsonMarkedAvailable() throws Exception {

        ResultActions rating = availablePartners.perform(get("/api/partner/ratings").param("title", "Kobzar"));
        ResultActions offer = availablePartners.perform(get("/api/partner/offers").param("title", "Kobzar"));

        rating.andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.average").isNumber());
        offer.andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.priceUah").isNumber());
    }

    @Test
    void ratings_partnerUnavailable_returns503() throws Exception {

        ResultActions result = unavailablePartners.perform(get("/api/partner/ratings").param("title", "Kobzar"));

        result.andExpect(status().isServiceUnavailable());
    }
}