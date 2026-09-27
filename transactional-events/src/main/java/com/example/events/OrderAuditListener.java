package com.example.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderAuditListener {
    private final AuditWriter auditWriter;

    public OrderAuditListener(AuditWriter auditWriter) {
        this.auditWriter = auditWriter;
    }

    @TransactionalEventListener
    public void on(OrderPlaced event) {
        auditWriter.record(event.orderId());
    }
}
