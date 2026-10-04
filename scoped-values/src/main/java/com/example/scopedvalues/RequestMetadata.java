package com.example.scopedvalues;

import java.util.Objects;

public record RequestMetadata(String requestId, String tenantId) {
    public RequestMetadata {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
    }
}
