package com.example.events;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase_order")
public class Order {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String status;

    protected Order() {
    }

    public Order(UUID id, String status) {
        this.id = id;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}
