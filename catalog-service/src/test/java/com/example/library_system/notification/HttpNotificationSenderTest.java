package com.example.library_system.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class HttpNotificationSenderTest {

    private MockRestServiceServer server;
    private HttpNotificationSender sender;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://notification");
        server = MockRestServiceServer.bindTo(builder).build();
        sender = new HttpNotificationSender(builder.build());
    }

    @Test
    void send_serviceAccepts_postsLetterAsJson() {

        server.expect(requestTo("http://notification/api/notifications"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"recipient\":\"readers@library.example\",\"subject\":\"Тема\",\"body\":\"Текст\"}"))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        assertDoesNotThrow(() -> sender.send("readers@library.example", "Тема", "Текст"));
        server.verify();
    }

    @Test
    void send_serviceUnavailable_throwsException() {

        server.expect(requestTo("http://notification/api/notifications"))
                .andRespond(withServiceUnavailable());

        assertThrows(RestClientException.class,
                () -> sender.send("readers@library.example", "Тема", "Текст"));
    }
}
