package com.example.library_system.config;

import com.example.library_system.partner.GrpcPartnerAdapter;
import com.example.library_system.partner.HttpPartnerAdapter;
import com.example.library_system.partner.PartnerHttpApi;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PartnerTransportConfig {

    public PartnerTransportConfig(PartnerTransportProperties transportProperties) {
        log.info("Partner transport: {}", transportProperties.transport());
    }

    @Bean
    @ConditionalOnProperty(prefix = "library.partner", name = "transport", havingValue = "http", matchIfMissing = true)
    public HttpPartnerAdapter httpPartnerAdapter(PartnerHttpApi partnerHttpApi, PartnerHttpProperties properties) {
        log.info("Partner HTTP adapter: {} (connect {}, read {})",
                properties.baseUrl(), properties.connectTimeout(), properties.readTimeout());
        return new HttpPartnerAdapter(partnerHttpApi);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "library.partner", name = "transport", havingValue = "grpc")
    public ManagedChannel partnerGrpcChannel(PartnerGrpcProperties properties) {
        return Grpc.newChannelBuilder(properties.target(), InsecureChannelCredentials.create()).build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "library.partner", name = "transport", havingValue = "grpc")
    public GrpcPartnerAdapter grpcPartnerAdapter(ManagedChannel partnerGrpcChannel, PartnerGrpcProperties properties) {
        log.info("Partner gRPC adapter: {} (deadline {})", properties.target(), properties.deadline());
        return new GrpcPartnerAdapter(partnerGrpcChannel, properties.deadline());
    }
}