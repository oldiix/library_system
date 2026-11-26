package com.example.library_system.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import com.example.contract.partner.BookRequest;
import com.example.contract.partner.DiscountReply;
import com.example.contract.partner.OfferReply;
import com.example.contract.partner.PartnerCatalogGrpc;
import com.example.contract.partner.RatingReply;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class GrpcPartnerAdapter implements RatingLookup, OfferLookup, DiscountLookup {

    private final PartnerCatalogGrpc.PartnerCatalogBlockingStub stub;
    private final Duration deadline;

    public GrpcPartnerAdapter(Channel channel, Duration deadline) {
        this.stub = PartnerCatalogGrpc.newBlockingStub(channel);
        this.deadline = deadline;
    }

    @Override
    public BookRating findRating(String isbn) {
        RatingReply reply = call(isbn, "GetRating", catalog -> catalog.getRating(request(isbn)));
        return new BookRating(reply.getAverage(), reply.getReviewsCount(), true);
    }

    @Override
    public StoreOffer findOffer(String isbn) {
        OfferReply reply = call(isbn, "GetOffer", catalog -> catalog.getOffer(request(isbn)));
        return new StoreOffer(new BigDecimal(reply.getPriceUah()), reply.getInStock(), true);
    }

    @Override
    public BookDiscount findDiscount(String isbn) {
        DiscountReply reply = call(isbn, "GetDiscount", catalog -> catalog.getDiscount(request(isbn)));
        return new BookDiscount(reply.getIsbn(), reply.getPercent());
    }

    private static BookRequest request(String isbn) {
        return BookRequest.newBuilder()
                .setIsbn(isbn == null ? "" : isbn)
                .build();
    }

    private <T> T call(String isbn, String method,
                       Function<PartnerCatalogGrpc.PartnerCatalogBlockingStub, T> rpc) {
        try {
            return rpc.apply(stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS));
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new PartnerDataNotFoundException("Партнер не знає книги з ISBN " + isbn, exception);
            }
            log.warn("gRPC-виклик partner-service ({} для {}) не вдався: {}", method, isbn, exception.getStatus());
            throw new PartnerUnavailableException("Партнер тимчасово недоступний", exception);
        }
    }
}