package com.example.partner.grpc;

import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcServerLifecycle implements SmartLifecycle {

    private final BindableService service;
    private final int configuredPort;
    private Server server;

    public GrpcServerLifecycle(BindableService service, int configuredPort) {
        this.service = service;
        this.configuredPort = configuredPort;
    }

    @Override
    public synchronized void start() {
        try {
            server = Grpc.newServerBuilderForPort(configuredPort, InsecureServerCredentials.create())
                    .addService(service)
                    .build()
                    .start();
            log.info("gRPC-сервер partner-service запущено на порту {}", server.getPort());
        } catch (IOException exception) {
            throw new IllegalStateException("Не вдалося запустити gRPC-сервер на порту " + configuredPort, exception);
        }
    }

    @Override
    public synchronized void stop() {
        if (server == null) {
            return;
        }
        server.shutdown();
        try {
            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException exception) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("gRPC-сервер partner-service зупинено");
        server = null;
    }

    @Override
    public synchronized boolean isRunning() {
        return server != null && !server.isShutdown();
    }

    public synchronized int getPort() {
        return server == null ? -1 : server.getPort();
    }
}