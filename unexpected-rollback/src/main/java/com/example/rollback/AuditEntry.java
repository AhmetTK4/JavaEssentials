package com.example.rollback;

import java.time.Instant;
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
    private String action;

    @Column(nullable = false)
    private String detail;

    @Column(nullable = false)
    private Instant createdAt;

    protected AuditEntry() {
    }

    public AuditEntry(UUID id, String action, String detail, Instant createdAt) {
        this.id = id;
        this.action = action;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
