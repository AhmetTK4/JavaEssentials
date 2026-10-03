package com.example.pagination;
import jakarta.persistence.*;
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;
    @Column(nullable = false)
    private String sku;
    private int quantity;
    protected OrderItem() {}
    OrderItem(PurchaseOrder order, String sku, int quantity) {
        this.order = order;
        this.sku = sku;
        this.quantity = quantity;
    }
    public Long getId() { return id; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
}
