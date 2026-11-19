package com.example.notification;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    @Test
    void findAll_processedEvents_areVisibleThroughEndpoint() throws Exception {
        NotificationStore store = new NotificationStore();
        UUID eventId = UUID.randomUUID();
        store.save(new Notification(eventId, "BookCreatedEvent", "readers@library.example",
                "Нова книга в бібліотеці: Kobzar", "Текст", Instant.now()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController(store)).build();

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value(eventId.toString()))
                .andExpect(jsonPath("$[0].eventType").value("BookCreatedEvent"))
                .andExpect(jsonPath("$[0].recipient").value("readers@library.example"));
    }

    @Test
    void findAll_nothingProcessed_returnsEmptyList() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController(new NotificationStore())).build();

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
