package com.example.library_system.aop;

import com.example.library_system.audit.AuditEntry;
import com.example.library_system.audit.AuditTrail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditProxyTest {

    private AuditTrail auditTrail;
    private SampleService proxy;

    @BeforeEach
    void setUp() {
        auditTrail = new AuditTrail();
        AspectJProxyFactory factory = new AspectJProxyFactory(new SampleService());
        factory.addAspect(new AuditAspect(auditTrail,
                Clock.fixed(Instant.parse("2026-10-22T10:00:00Z"), ZoneOffset.UTC)));
        proxy = factory.getProxy();
    }

    @Test
    void callThroughProxy_methodSucceeds_auditEntryRecorded() {

        String result = proxy.succeed("Kobzar");

        assertThat(result).isEqualTo("saved: Kobzar");
        List<AuditEntry> entries = auditTrail.findAll();
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().action()).isEqualTo("SAMPLE_SAVED");
        assertThat(entries.getFirst().method()).contains("succeed");
        assertThat(entries.getFirst().arguments()).isEqualTo("[Kobzar]");
    }

    @Test
    void callThroughProxy_methodThrows_noAuditEntryRecorded() {

        assertThrows(IllegalStateException.class, () -> proxy.fail());

        assertThat(auditTrail.findAll()).isEmpty();
    }

    static class SampleService {

        @Audited(action = "SAMPLE_SAVED")
        public String succeed(String title) {
            return "saved: " + title;
        }

        @Audited(action = "SAMPLE_FAILED")
        public void fail() {
            throw new IllegalStateException("Помилка збереження");
        }
    }
}