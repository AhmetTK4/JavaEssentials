package com.example.scopedvalues;

import com.example.scopedvalues.OrderSummaryService.OrderSummary;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public final class VirtualThreadRequestRunner {
    private VirtualThreadRequestRunner() {
    }

    public static void main(String[] args) throws Exception {
        OrderSummaryService service = new OrderSummaryService(new InventoryClient());
        List<Request> requests = List.of(
                new Request("req-101", "tenant-blue", "SKU-CHAIR"),
                new Request("req-102", "tenant-green", "SKU-DESK"),
                new Request("req-103", "tenant-orange", "SKU-LAMP"));

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<OrderSummary>> tasks = requests.stream()
                    .<Callable<OrderSummary>>map(request -> () -> RequestContext.callWith(
                            new RequestMetadata(request.requestId(), request.tenantId()),
                            () -> service.summarize(request.sku())))
                    .toList();

            executor.invokeAll(tasks).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception exception) {
                            throw new IllegalStateException("Request failed", exception);
                        }
                    })
                    .forEach(System.out::println);
        }

        System.out.println("Context bound after requests: " + RequestContext.isBound());
    }

    private record Request(String requestId, String tenantId, String sku) {
    }
}
