package com.example.library_system.external;

import java.time.Duration;

final class SimulatedLatency {

    private SimulatedLatency() {
    }

    static void pause(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Очікування відповіді перервано", e);
        }
    }
}