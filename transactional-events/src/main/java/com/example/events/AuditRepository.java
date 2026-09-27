package com.example.events;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditEntry, UUID> {
    boolean existsByOrderId(UUID orderId);
}
