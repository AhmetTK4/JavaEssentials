package com.example.events;

import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final AuditRepository audits;
    private final OutboxRepository outbox;
    private final ApplicationEventPublisher events;

    public OrderService(OrderRepository orders, AuditRepository audits,
                        OutboxRepository outbox, ApplicationEventPublisher events) {
        this.orders = orders;
        this.audits = audits;
        this.outbox = outbox;
        this.events = events;
    }

    @Transactional
    public UUID placeOrder() {
        UUID id = UUID.randomUUID();
        orders.save(new Order(id, "PLACED"));
        events.publishEvent(new OrderPlaced(id));
        return id;
    }

    // Alternative: both writes succeed or fail in the original transaction.
    @Transactional
    public UUID placeOrderWithAtomicAudit() {
        UUID id = UUID.randomUUID();
        orders.save(new Order(id, "PLACED"));
        audits.save(new AuditEntry(UUID.randomUUID(), id, "ORDER_PLACED"));
        return id;
    }

    // Alternative: record pending delivery before the original commit.
    @Transactional
    public UUID placeOrderWithOutbox() {
        UUID id = UUID.randomUUID();
        orders.save(new Order(id, "PLACED"));
        outbox.save(new OutboxEvent(UUID.randomUUID(), id, "OrderPlaced"));
        return id;
    }
}
