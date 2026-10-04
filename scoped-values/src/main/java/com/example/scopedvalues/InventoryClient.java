package com.example.scopedvalues;

public final class InventoryClient {
    public StockSnapshot findStock(String sku) {
        RequestMetadata metadata = RequestContext.current();
        return new StockSnapshot(sku, 12, metadata.tenantId(), metadata.requestId());
    }

    public record StockSnapshot(String sku, int available,
                                String tenantId, String observedRequestId) {
    }
}
