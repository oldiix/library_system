package com.example.contract.partner;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartnerContractTest {

    @Test
    void generatedService_hasThreeUnaryMethods() {
        assertThat(PartnerCatalogGrpc.getServiceDescriptor().getMethods())
                .extracting(MethodDescriptor::getFullMethodName)
                .containsExactlyInAnyOrder(
                        "library.partner.v1.PartnerCatalog/GetRating",
                        "library.partner.v1.PartnerCatalog/GetOffer",
                        "library.partner.v1.PartnerCatalog/GetDiscount");
        assertThat(PartnerCatalogGrpc.getGetRatingMethod().getType()).isEqualTo(MethodDescriptor.MethodType.UNARY);
    }

    @Test
    void generatedMessages_serializeAndParseBack() throws InvalidProtocolBufferException {
        OfferReply offer = OfferReply.newBuilder()
                .setPriceUah("420.00")
                .setInStock(true)
                .build();

        OfferReply parsed = OfferReply.parseFrom(offer.toByteArray());

        assertThat(parsed).isEqualTo(offer);
        assertThat(parsed.getPriceUah()).isEqualTo("420.00");
    }

    @Test
    void emptyRequest_hasDefaultValuesInsteadOfNull() {
        BookRequest request = BookRequest.getDefaultInstance();

        assertThat(request.getIsbn()).isEmpty();
    }
}