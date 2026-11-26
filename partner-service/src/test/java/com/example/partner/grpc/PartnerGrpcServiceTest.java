package com.example.partner.grpc;

import com.example.contract.partner.BookRequest;
import com.example.contract.partner.DiscountReply;
import com.example.contract.partner.OfferReply;
import com.example.contract.partner.PartnerCatalogGrpc;
import com.example.contract.partner.RatingReply;
import com.example.partner.BookstoreService;
import com.example.partner.PartnerCatalog;
import com.example.partner.ReviewsService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartnerGrpcServiceTest {

    private static final String KOBZAR = "978-966-01-0001-1";

    private Server server;
    private ManagedChannel channel;

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    private PartnerCatalogGrpc.PartnerCatalogBlockingStub partners(boolean available) throws IOException {
        PartnerCatalog catalog = new PartnerCatalog();
        PartnerGrpcService service = new PartnerGrpcService(
                new ReviewsService(catalog, Duration.ZERO, available),
                new BookstoreService(catalog, Duration.ZERO, available));
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name).directExecutor().addService(service).build().start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        return PartnerCatalogGrpc.newBlockingStub(channel);
    }

    @Test
    void knownIsbn_ratingOfferAndDiscount_areReturned() throws IOException {
        PartnerCatalogGrpc.PartnerCatalogBlockingStub stub = partners(true);
        BookRequest request = BookRequest.newBuilder().setIsbn(KOBZAR).build();

        RatingReply rating = stub.getRating(request);
        OfferReply offer = stub.getOffer(request);
        DiscountReply discount = stub.getDiscount(request);

        assertThat(rating.getAverage()).isEqualTo(4.8);
        assertThat(rating.getReviewsCount()).isEqualTo(312);
        assertThat(offer.getPriceUah()).isEqualTo("420.00");
        assertThat(offer.getInStock()).isTrue();
        assertThat(discount.getPercent()).isEqualTo(10);
    }

    @Test
    void unknownIsbn_returnsNotFound() throws IOException {
        PartnerCatalogGrpc.PartnerCatalogBlockingStub stub = partners(true);
        BookRequest request = BookRequest.newBuilder().setIsbn("978-0-00-000000-0").build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
                () -> stub.getOffer(request));

        assertThat(exception.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        assertThat(exception.getStatus().getDescription()).contains("978-0-00-000000-0");
    }

    @Test
    void emptyIsbn_returnsNotFound() throws IOException {
        PartnerCatalogGrpc.PartnerCatalogBlockingStub stub = partners(true);

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
                () -> stub.getRating(BookRequest.getDefaultInstance()));

        assertThat(exception.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void partnerUnavailable_returnsUnavailable() throws IOException {
        PartnerCatalogGrpc.PartnerCatalogBlockingStub stub = partners(false);
        BookRequest request = BookRequest.newBuilder().setIsbn(KOBZAR).build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
                () -> stub.getDiscount(request));

        assertThat(exception.getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
    }
}