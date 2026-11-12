package com.example.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new NotificationController(new EmailService(Duration.ZERO))).build();
    }

    @Test
    void send_validLetter_returns202AndLetterAppearsInSentList() throws Exception {

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"readers@library.example\",\"subject\":\"Нова книга\",\"body\":\"Текст\"}"))
                .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipient").value("readers@library.example"))
                .andExpect(jsonPath("$[0].subject").value("Нова книга"));
    }

    @Test
    void send_blankRecipient_returns400() throws Exception {

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"\",\"subject\":\"Нова книга\",\"body\":\"Текст\"}"))
                .andExpect(status().isBadRequest());
    }
}
