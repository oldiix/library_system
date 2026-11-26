package com.example.partner.grpc;

import com.example.partner.BookstoreService;
import com.example.partner.ReviewsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "partner.grpc", name = "enabled", havingValue = "true")
public class GrpcServerConfig {

    @Bean
    public PartnerGrpcService partnerGrpcService(ReviewsService reviewsService, BookstoreService bookstoreService) {
        return new PartnerGrpcService(reviewsService, bookstoreService);
    }

    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(PartnerGrpcService partnerGrpcService,
                                                   @Value("${partner.grpc.port:9092}") int port) {
        return new GrpcServerLifecycle(partnerGrpcService, port);
    }
}