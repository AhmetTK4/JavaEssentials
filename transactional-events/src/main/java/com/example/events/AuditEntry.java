package com.example.events;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_entry")
public class AuditEntry {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String action;

    protected AuditEntry() {
    }

    public AuditEntry(UUID id, UUID orderId, String action) {
        this.id = id;
        this.orderId = orderId;
        this.action = action;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getAction() {
        return action;
    }
}
