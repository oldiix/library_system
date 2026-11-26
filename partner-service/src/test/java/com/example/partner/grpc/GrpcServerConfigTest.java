package com.example.partner.grpc;

import com.example.partner.BookstoreService;
import com.example.partner.PartnerCatalog;
import com.example.partner.ReviewsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class GrpcServerConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(GrpcServerConfig.class)
            .withBean(ReviewsService.class, () -> new ReviewsService(new PartnerCatalog(), Duration.ZERO, true))
            .withBean(BookstoreService.class, () -> new BookstoreService(new PartnerCatalog(), Duration.ZERO, true));

    @Test
    void grpcServer_disabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(GrpcServerLifecycle.class);
            assertThat(context).doesNotHaveBean(PartnerGrpcService.class);
        });
    }

    @Test
    void grpcServer_explicitlyDisabled_isNotCreated() {
        contextRunner.withPropertyValues("partner.grpc.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(GrpcServerLifecycle.class));
    }

    @Test
    void grpcServer_enabled_startsWithContextOnConfiguredPortAndStopsWithIt() {
        GrpcServerLifecycle[] started = new GrpcServerLifecycle[1];

        contextRunner.withPropertyValues("partner.grpc.enabled=true", "partner.grpc.port=0")
                .run(context -> {
                    assertThat(context).hasSingleBean(GrpcServerLifecycle.class);
                    started[0] = context.getBean(GrpcServerLifecycle.class);
                    assertThat(started[0].isRunning()).isTrue();
                });

        assertThat(started[0].isRunning()).isFalse();
    }
}