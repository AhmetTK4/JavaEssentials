package com.example.scopedvalues;

import java.util.concurrent.Callable;

public final class RequestContext {
    private static final ScopedValue<RequestMetadata> CURRENT = ScopedValue.newInstance();

    private RequestContext() {
    }

    public static RequestMetadata current() {
        return CURRENT.get();
    }

    public static boolean isBound() {
        return CURRENT.isBound();
    }

    public static <T> T callWith(RequestMetadata metadata, Callable<T> operation) throws Exception {
        return ScopedValue.where(CURRENT, metadata).call(operation::call);
    }
}
