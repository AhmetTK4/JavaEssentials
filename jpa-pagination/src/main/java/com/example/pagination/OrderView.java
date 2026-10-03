package com.example.pagination;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
public record OrderView(Long id, Instant createdAt, List<ItemView> items) {
    static OrderView from(PurchaseOrder order) {
        List<ItemView> items = order.getItems().stream()
            .sorted(Comparator.comparing(OrderItem::getId))
            .map(item -> new ItemView(item.getId(), item.getSku(), item.getQuantity()))
            .toList();
        return new OrderView(order.getId(), order.getCreatedAt(), items);
    }
}
