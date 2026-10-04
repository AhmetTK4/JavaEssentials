package com.example.scopedvalues;

import com.example.scopedvalues.OrderSummaryService.OrderSummary;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualThreadRequestRunnerTest {
    @Test
    void isolatesConcurrentRequestContextsOnVirtualThreads() throws Exception {
        OrderSummaryService service = new OrderSummaryService(new InventoryClient());
        List<Callable<OrderSummary>> tasks = new ArrayList<>();

        for (int index = 0; index < 100; index++) {
            int requestNumber = index;
            tasks.add(() -> RequestContext.callWith(
                    new RequestMetadata("req-" + requestNumber, "tenant-" + requestNumber),
                    () -> service.summarize("sku-" + requestNumber)));
        }

        List<OrderSummary> summaries;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            summaries = executor.invokeAll(tasks).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception exception) {
                            throw new AssertionError(exception);
                        }
                    })
                    .toList();
        }

        assertEquals(100, summaries.size());
        for (int index = 0; index < summaries.size(); index++) {
            OrderSummary summary = summaries.get(index);
            assertEquals("req-" + index, summary.requestId());
            assertEquals("tenant-" + index, summary.tenantId());
            assertEquals("sku-" + index, summary.sku());
            assertTrue(summary.handledByVirtualThread());
        }
        assertFalse(RequestContext.isBound());
    }
}
