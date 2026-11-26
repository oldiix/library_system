package com.example.library_system.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import com.example.contract.partner.BookRequest;
import com.example.contract.partner.DiscountReply;
import com.example.contract.partner.OfferReply;
import com.example.contract.partner.PartnerCatalogGrpc;
import com.example.contract.partner.RatingReply;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GrpcPartnerAdapterTest {

    private static final String KOBZAR = "978-966-01-0001-1";

    private FakePartner fakePartner;
    private Server server;
    private ManagedChannel channel;
    private GrpcPartnerAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        String name = InProcessServerBuilder.generateName();
        fakePartner = new FakePartner();
        server = InProcessServerBuilder.forName(name).directExecutor().addService(fakePartner).build().start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        adapter = new GrpcPartnerAdapter(channel, Duration.ofSeconds(2));
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void partnerResponds_repliesAreMappedToCommonDtos() {
        BookRating rating = adapter.findRating(KOBZAR);
        StoreOffer offer = adapter.findOffer(KOBZAR);
        BookDiscount discount = adapter.findDiscount(KOBZAR);

        assertThat(fakePartner.lastIsbn).isEqualTo(KOBZAR);
        assertThat(rating.average()).isEqualTo(4.8);
        assertThat(rating.reviewsCount()).isEqualTo(312);
        assertThat(rating.available()).isTrue();
        assertThat(offer.priceUah()).isEqualByComparingTo("420.00");
        assertThat(offer.inStock()).isTrue();
        assertThat(discount.isbn()).isEqualTo(KOBZAR);
        assertThat(discount.percent()).isEqualTo(10);
    }

    @Test
    void notFoundStatus_becomesPartnerDataNotFound() {
        fakePartner.failure = Status.NOT_FOUND.withDescription("unknown book");

        PartnerDataNotFoundException exception = assertThrows(PartnerDataNotFoundException.class,
                () -> adapter.findOffer(KOBZAR));

        assertThat(exception.getMessage()).contains(KOBZAR);
    }

    @Test
    void unavailableStatus_becomesPartnerUnavailableAndHidesGrpcException() {
        fakePartner.failure = Status.UNAVAILABLE.withDescription("partner is down");

        PartnerUnavailableException exception = assertThrows(PartnerUnavailableException.class,
                () -> adapter.findRating(KOBZAR));

        assertThat(exception).isNotInstanceOf(StatusRuntimeException.class);
        assertThat(exception.getCause()).isInstanceOf(StatusRuntimeException.class);
    }

    @Test
    void serverStopped_becomesPartnerUnavailable() throws InterruptedException {
        server.shutdownNow();
        server.awaitTermination(1, TimeUnit.SECONDS);

        assertThrows(PartnerUnavailableException.class, () -> adapter.findDiscount(KOBZAR));
    }

    @Test
    void silentPartner_deadlineExceeded_becomesPartnerUnavailable() {
        fakePartner.silent = true;
        GrpcPartnerAdapter shortDeadline = new GrpcPartnerAdapter(channel, Duration.ofMillis(200));

        long started = System.nanoTime();
        PartnerUnavailableException exception = assertThrows(PartnerUnavailableException.class,
                () -> shortDeadline.findOffer(KOBZAR));
        Duration waited = Duration.ofNanos(System.nanoTime() - started);

        assertThat(waited).isLessThan(Duration.ofSeconds(2));
        StatusRuntimeException cause = (StatusRuntimeException) exception.getCause();
        assertThat(cause.getStatus().getCode()).isEqualTo(Status.Code.DEADLINE_EXCEEDED);
    }

    @Test
    void everyCallGetsItsOwnDeadline() throws InterruptedException {
        GrpcPartnerAdapter shortDeadline = new GrpcPartnerAdapter(channel, Duration.ofMillis(300));

        shortDeadline.findRating(KOBZAR);
        Thread.sleep(400);
        BookRating second = shortDeadline.findRating(KOBZAR);

        assertThat(second.reviewsCount()).isEqualTo(312);
    }

    static class FakePartner extends PartnerCatalogGrpc.PartnerCatalogImplBase {

        volatile Status failure;
        volatile boolean silent;
        volatile String lastIsbn;

        @Override
        public void getRating(BookRequest request, StreamObserver<RatingReply> responseObserver) {
            reply(request, responseObserver, RatingReply.newBuilder().setAverage(4.8).setReviewsCount(312).build());
        }

        @Override
        public void getOffer(BookRequest request, StreamObserver<OfferReply> responseObserver) {
            reply(request, responseObserver, OfferReply.newBuilder().setPriceUah("420.00").setInStock(true).build());
        }

        @Override
        public void getDiscount(BookRequest request, StreamObserver<DiscountReply> responseObserver) {
            reply(request, responseObserver,
                    DiscountReply.newBuilder().setIsbn(request.getIsbn()).setPercent(10).build());
        }

        private <T> void reply(BookRequest request, StreamObserver<T> responseObserver, T value) {
            lastIsbn = request.getIsbn();
            if (silent) {
                return;
            }
            if (failure != null) {
                responseObserver.onError(failure.asRuntimeException());
                return;
            }
            responseObserver.onNext(value);
            responseObserver.onCompleted();
        }
    }
}