package com.example.library_system.config;

import com.example.library_system.partner.DiscountLookup;
import com.example.library_system.partner.GrpcPartnerAdapter;
import com.example.library_system.partner.HttpPartnerAdapter;
import com.example.library_system.partner.OfferLookup;
import com.example.library_system.partner.RatingLookup;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PartnerTransportConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(RestClient.Builder.class, RestClient::builder)
            .withUserConfiguration(PropertiesConfig.class, HttpClientsConfig.class, PartnerTransportConfig.class)
            .withPropertyValues(
                    "library.partner.http.base-url=http://partner.test:8082",
                    "library.partner.http.connect-timeout=1s",
                    "library.partner.http.read-timeout=2s",
                    "library.partner.grpc.target=localhost:9092",
                    "library.partner.grpc.deadline=2s");

    @Test
    void noTransportProperty_usesHttpAdapter() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RatingLookup.class);
            assertThat(context.getBean(RatingLookup.class)).isInstanceOf(HttpPartnerAdapter.class);
            assertThat(context).doesNotHaveBean(ManagedChannel.class);
        });
    }

    @Test
    void httpTransport_usesHttpAdapter() {
        contextRunner.withPropertyValues("library.partner.transport=http").run(context -> {
            assertThat(context).hasSingleBean(HttpPartnerAdapter.class);
            assertThat(context).doesNotHaveBean(GrpcPartnerAdapter.class);
        });
    }

    @Test
    void grpcTransport_oneGrpcAdapterServesAllThreePorts() {
        contextRunner.withPropertyValues("library.partner.transport=grpc").run(context -> {
            assertThat(context).hasSingleBean(RatingLookup.class);
            assertThat(context.getBean(RatingLookup.class)).isInstanceOf(GrpcPartnerAdapter.class);
            assertThat(context.getBean(OfferLookup.class)).isSameAs(context.getBean(DiscountLookup.class));
            assertThat(context).doesNotHaveBean(HttpPartnerAdapter.class);
        });
    }

    @Test
    void grpcChannel_isShutDownTogetherWithContext() {
        AtomicReference<ManagedChannel> channel = new AtomicReference<>();

        contextRunner.withPropertyValues("library.partner.transport=GRPC")
                .run(context -> channel.set(context.getBean(ManagedChannel.class)));

        assertThat(channel.get().isShutdown()).isTrue();
    }

    @Test
    void unknownTransport_failsAtStartup() {
        contextRunner.withPropertyValues("library.partner.transport=soap").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("library.partner.transport");
        });
    }

    @EnableConfigurationProperties({PartnerTransportProperties.class,
            PartnerHttpProperties.class,
            PartnerGrpcProperties.class})
    static class PropertiesConfig {
    }
}