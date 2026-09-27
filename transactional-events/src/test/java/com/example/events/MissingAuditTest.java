package com.example.events;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(MissingAuditTest.BrokenListenerConfiguration.class)
class MissingAuditTest {
    @Autowired OrderRepository orders;
    @Autowired AuditRepository audits;
    @Autowired ApplicationEventPublisher events;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired BrokenAuditListener listener;

    @AfterEach
    void cleanUpCommittedRows() {
        audits.deleteAll();
        orders.deleteAll();
    }

    @Test
    void saveReturnsButAuditDoesNotCommit() {
        listener.completedCalls.set(0);
        UUID id = UUID.randomUUID();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            orders.save(new Order(id, "PLACED"));
            events.publishEvent(new BrokenOrderPlaced(id));
        });

        // The counter advances only after save() returns.
        assertThat(listener.completedCalls.get()).isEqualTo(1);
        assertThat(orders.existsById(id)).isTrue();
        assertThat(audits.existsByOrderId(id)).isFalse();
    }

    record BrokenOrderPlaced(UUID orderId) {}

    @TestConfiguration(proxyBeanMethods = false)
    static class BrokenListenerConfiguration {
        @Bean
        BrokenAuditListener brokenAuditListener(AuditRepository audits) {
            return new BrokenAuditListener(audits);
        }
    }

    // Deliberately incorrect: isolated to a test so application code uses the fix.
    static class BrokenAuditListener {
        private final AuditRepository audits;
        final AtomicInteger completedCalls = new AtomicInteger();

        BrokenAuditListener(AuditRepository audits) {
            this.audits = audits;
        }

        @TransactionalEventListener
        public void on(BrokenOrderPlaced event) {
            audits.save(new AuditEntry(UUID.randomUUID(), event.orderId(), "ORDER_PLACED"));
            completedCalls.incrementAndGet();
        }
    }
}
