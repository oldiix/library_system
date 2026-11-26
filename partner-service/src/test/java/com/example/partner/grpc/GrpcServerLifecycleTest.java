package com.example.partner.grpc;

import com.example.contract.partner.BookRequest;
import com.example.contract.partner.PartnerCatalogGrpc;
import com.example.partner.BookstoreService;
import com.example.partner.PartnerCatalog;
import com.example.partner.ReviewsService;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class GrpcServerLifecycleTest {

    @Test
    void startAndStop_serverAnswersOnRealPortWhileRunning() throws InterruptedException {
        PartnerCatalog catalog = new PartnerCatalog();
        GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(new PartnerGrpcService(
                new ReviewsService(catalog, Duration.ZERO, true),
                new BookstoreService(catalog, Duration.ZERO, true)), 0);

        lifecycle.start();
        ManagedChannel channel = Grpc.newChannelBuilderForAddress(
                "localhost", lifecycle.getPort(), InsecureChannelCredentials.create()).build();
        try {
            assertThat(lifecycle.isRunning()).isTrue();
            assertThat(lifecycle.getPort()).isPositive();
            assertThat(PartnerCatalogGrpc.newBlockingStub(channel)
                    .getRating(BookRequest.newBuilder().setIsbn("978-966-01-0002-8").build())
                    .getReviewsCount()).isEqualTo(154);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            lifecycle.stop();
        }

        assertThat(lifecycle.isRunning()).isFalse();
        assertThat(lifecycle.getPort()).isEqualTo(-1);
    }
}