package com.example.pagination;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class OrderQueryService {
    private final OrderRepository orders;
    public OrderQueryService(OrderRepository orders) { this.orders = orders; }
    @Transactional(readOnly = true)
    public Page<OrderView> find(OrderStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                "page must be >= 0; size must be between 1 and 100");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> ids = orders.findPageIds(status, pageable);
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, ids.getTotalElements());
        }
        Map<Long, PurchaseOrder> byId = orders.findWithItemsByIds(ids.getContent())
            .stream().collect(Collectors.toMap(PurchaseOrder::getId, Function.identity()));
        List<OrderView> content = ids.getContent().stream().map(id -> {
            PurchaseOrder order = byId.get(id);
            if (order == null) {
                throw new IllegalStateException("Order disappeared while reading page: " + id);
            }
            return OrderView.from(order);
        }).toList();
        return new PageImpl<>(content, pageable, ids.getTotalElements());
    }
}
