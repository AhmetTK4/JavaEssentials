package com.example.events;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderTransactionsTest {
    @Autowired OrderService service;
    @Autowired OrderRepository orders;
    @Autowired AuditRepository audits;
    @Autowired OutboxRepository outbox;
    @Autowired ApplicationEventPublisher events;
    @Autowired PlatformTransactionManager transactionManager;

    // No test-level @Transactional: each assertion below observes a real commit.
    @AfterEach
    void cleanUpCommittedRows() {
        outbox.deleteAll();
        audits.deleteAll();
        orders.deleteAll();
    }

    @Test
    void auditIsWrittenOnlyAfterCommit() {
        UUID id = tx().execute(status -> {
            UUID orderId = service.placeOrder();
            assertThat(audits.existsByOrderId(orderId)).isFalse();
            return orderId;
        });

        assertThat(orders.existsById(id)).isTrue();
        assertThat(audits.existsByOrderId(id)).isTrue();
    }

    @Test
    void rollbackDoesNotInvokeAfterCommitListener() {
        UUID id = tx().execute(status -> {
            UUID orderId = service.placeOrder();
            status.setRollbackOnly();
            return orderId;
        });

        assertThat(orders.existsById(id)).isFalse();
        assertThat(audits.existsByOrderId(id)).isFalse();
    }

    @Test
    void eventWithoutTransactionIsIgnored() {
        UUID id = UUID.randomUUID();
        events.publishEvent(new OrderPlaced(id));
        assertThat(audits.existsByOrderId(id)).isFalse();
    }

    @Test
    void atomicAuditCommitsWithOrder() {
        UUID id = service.placeOrderWithAtomicAudit();
        assertThat(orders.existsById(id)).isTrue();
        assertThat(audits.existsByOrderId(id)).isTrue();
    }

    @Test
    void atomicAuditRollsBackWithOrder() {
        UUID id = tx().execute(status -> {
            UUID orderId = service.placeOrderWithAtomicAudit();
            // Flush real SQL before rollback, rather than testing only queued entities.
            orders.flush();
            assertThat(audits.existsByOrderId(orderId)).isTrue();
            status.setRollbackOnly();
            return orderId;
        });

        assertThat(orders.existsById(id)).isFalse();
        assertThat(audits.existsByOrderId(id)).isFalse();
    }

    @Test
    void outboxCommitsPendingEventWithOrder() {
        UUID id = service.placeOrderWithOutbox();
        assertThat(orders.existsById(id)).isTrue();
        assertThat(outbox.findAll()).singleElement().satisfies(event -> {
            assertThat(event.getAggregateId()).isEqualTo(id);
            assertThat(event.getEventType()).isEqualTo("OrderPlaced");
            assertThat(event.getCreatedAt()).isNotNull();
            assertThat(event.getPublishedAt()).isNull();
        });
    }

    @Test
    void outboxRollsBackWithOrder() {
        UUID id = tx().execute(status -> {
            UUID orderId = service.placeOrderWithOutbox();
            orders.flush();
            assertThat(outbox.existsByAggregateId(orderId)).isTrue();
            status.setRollbackOnly();
            return orderId;
        });

        assertThat(orders.existsById(id)).isFalse();
        assertThat(outbox.existsByAggregateId(id)).isFalse();
    }

    private TransactionTemplate tx() {
        return new TransactionTemplate(transactionManager);
    }
}
