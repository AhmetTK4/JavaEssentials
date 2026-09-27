package com.example.events;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditWriter {
    private final AuditRepository audits;

    public AuditWriter(AuditRepository audits) {
        this.audits = audits;
    }

    // Called through a separate Spring bean so the transaction proxy is used.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID orderId) {
        audits.save(new AuditEntry(UUID.randomUUID(), orderId, "ORDER_PLACED"));
    }
}
