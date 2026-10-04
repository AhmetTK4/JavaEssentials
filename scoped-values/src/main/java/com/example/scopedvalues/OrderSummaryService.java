package com.example.scopedvalues;

import com.example.scopedvalues.InventoryClient.StockSnapshot;

public final class OrderSummaryService {
    private final InventoryClient inventoryClient;

    public OrderSummaryService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    public OrderSummary summarize(String sku) {
        StockSnapshot stock = inventoryClient.findStock(sku);
        return new OrderSummary(
                stock.sku(),
                stock.available(),
                stock.tenantId(),
                stock.observedRequestId(),
                Thread.currentThread().isVirtual());
    }

    public record OrderSummary(String sku, int available, String tenantId,
                               String requestId, boolean handledByVirtualThread) {
    }
}
