package com.example.pagination;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Instant createdAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OrderStatus status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    protected PurchaseOrder() {}
    public PurchaseOrder(Instant createdAt, OrderStatus status) {
        this.createdAt = createdAt;
        this.status = status;
    }
    public void addItem(String sku, int quantity) {
        items.add(new OrderItem(this, sku, quantity));
    }
    public Long getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
