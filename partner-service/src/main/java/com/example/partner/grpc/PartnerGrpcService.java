package com.example.partner.grpc;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import com.example.contract.partner.BookRequest;
import com.example.contract.partner.DiscountReply;
import com.example.contract.partner.OfferReply;
import com.example.contract.partner.PartnerCatalogGrpc;
import com.example.contract.partner.RatingReply;
import com.example.partner.BookNotInCatalogException;
import com.example.partner.BookstoreService;
import com.example.partner.PartnerTemporarilyUnavailableException;
import com.example.partner.ReviewsService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class PartnerGrpcService extends PartnerCatalogGrpc.PartnerCatalogImplBase {

    private final ReviewsService reviewsService;
    private final BookstoreService bookstoreService;

    @Override
    public void getRating(BookRequest request, StreamObserver<RatingReply> responseObserver) {
        respond(responseObserver, () -> {
            BookRating rating = reviewsService.ratingFor(request.getIsbn());
            return RatingReply.newBuilder()
                    .setAverage(rating.average())
                    .setReviewsCount(rating.reviewsCount())
                    .build();
        });
    }

    @Override
    public void getOffer(BookRequest request, StreamObserver<OfferReply> responseObserver) {
        respond(responseObserver, () -> {
            StoreOffer offer = bookstoreService.offerFor(request.getIsbn());
            return OfferReply.newBuilder()
                    .setPriceUah(offer.priceUah().toPlainString())
                    .setInStock(offer.inStock())
                    .build();
        });
    }

    @Override
    public void getDiscount(BookRequest request, StreamObserver<DiscountReply> responseObserver) {
        respond(responseObserver, () -> {
            BookDiscount discount = bookstoreService.discountFor(request.getIsbn());
            return DiscountReply.newBuilder()
                    .setIsbn(discount.isbn())
                    .setPercent(discount.percent())
                    .build();
        });
    }

    private <T> void respond(StreamObserver<T> responseObserver, Supplier<T> call) {
        try {
            responseObserver.onNext(call.get());
            responseObserver.onCompleted();
        } catch (BookNotInCatalogException exception) {
            log.info("gRPC NOT_FOUND: {}", exception.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(exception.getMessage())
                    .asRuntimeException());
        } catch (PartnerTemporarilyUnavailableException exception) {
            log.warn("gRPC UNAVAILABLE: {}", exception.getMessage());
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription(exception.getMessage())
                    .asRuntimeException());
        }
    }
}