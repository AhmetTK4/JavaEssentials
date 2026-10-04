package com.example.scopedvalues;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThreadLocalLeakTest {
    private static final ThreadLocal<String> LEGACY_REQUEST_ID = new ThreadLocal<>();

    @Test
    void demonstratesHowPooledThreadsCanExposeStaleState() throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> LEGACY_REQUEST_ID.set("request-a")).get();

            String valueSeenByUnrelatedTask = executor.submit(LEGACY_REQUEST_ID::get).get();

            assertEquals("request-a", valueSeenByUnrelatedTask);
            executor.submit(LEGACY_REQUEST_ID::remove).get();
        }
    }
}
