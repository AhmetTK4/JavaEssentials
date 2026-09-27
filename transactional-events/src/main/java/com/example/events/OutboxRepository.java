package com.example.events;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    boolean existsByAggregateId(UUID aggregateId);
}
