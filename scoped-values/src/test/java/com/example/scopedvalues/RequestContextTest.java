package com.example.scopedvalues;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestContextTest {
    @Test
    void exposesTheBindingToDeepCallees() throws Exception {
        RequestMetadata metadata = new RequestMetadata("req-1", "tenant-a");

        String requestId = RequestContext.callWith(metadata, this::readFromDeepCall);

        assertEquals("req-1", requestId);
        assertFalse(RequestContext.isBound());
    }

    @Test
    void automaticallyUnbindsWhenTheOperationFails() {
        RequestMetadata metadata = new RequestMetadata("req-2", "tenant-b");

        assertThrows(IllegalStateException.class,
                () -> RequestContext.callWith(metadata, () -> {
                    throw new IllegalStateException("downstream failure");
                }));

        assertFalse(RequestContext.isBound());
        assertThrows(NoSuchElementException.class, RequestContext::current);
    }

    @Test
    void nestedBindingRestoresTheOuterValue() throws Exception {
        RequestMetadata outer = new RequestMetadata("req-outer", "tenant-a");
        RequestMetadata inner = new RequestMetadata("req-inner", "tenant-b");

        RequestContext.callWith(outer, () -> {
            assertEquals(outer, RequestContext.current());
            RequestContext.callWith(inner, () -> {
                assertEquals(inner, RequestContext.current());
                return null;
            });
            assertEquals(outer, RequestContext.current());
            return null;
        });

        assertFalse(RequestContext.isBound());
    }

    private String readFromDeepCall() {
        return RequestContext.current().requestId();
    }
}
