# Java 25 Scoped Values with Virtual Threads

Java 25 • Maven • JUnit 5 • no framework or external service

A runnable companion project for **Java 25 Scoped Values: The Safer Alternative to ThreadLocal for Virtual Threads**. The article will link here when published.

The example passes an immutable request ID and tenant ID through a controller-like call chain without method-parameter plumbing. Each simulated request runs on its own virtual thread. `ScopedValue` keeps the context readable only inside a bounded operation and automatically removes the binding after success or failure.

## Run it

Install JDK **25** and Maven **3.6.3+**, then run from this directory:

```sh
mvn clean verify
mvn exec:java -Dexec.mainClass=com.example.scopedvalues.VirtualThreadRequestRunner -Dexec.classpathScope=runtime
```

No preview flags are needed: `ScopedValue` is final in Java 25, and virtual threads are final since Java 21.

## Request flow

```text
virtual thread
  └─ RequestContext.callWith(request metadata)
       └─ OrderSummaryService.summarize(sku)
            └─ InventoryClient.findStock(sku)
                 └─ RequestContext.current()
```

`RequestContext` owns the private `ScopedValue`, so downstream classes can read a binding but cannot replace it directly. `RequestMetadata` is immutable. `VirtualThreadRequestRunner` creates a fresh virtual thread per task and establishes the binding inside that task.

## What the tests prove

- A deeply nested callee can read request metadata without every intermediate method accepting it.
- The binding disappears after both normal completion and an exception.
- A nested binding temporarily shadows and then restores the outer value.
- One hundred concurrent virtual-thread requests retain the correct request and tenant IDs.
- A deliberately unsafe `ThreadLocal` example shows stale state on a reused pool thread.

Run only the leak demonstration with `mvn -Dtest=ThreadLocalLeakTest test`. The test documents a failure mode; its assertion passes when the stale value is observed. Production code should always remove a `ThreadLocal` in a `finally` block when migration is not possible.

## When to use `ScopedValue`

It fits one-way, read-mostly context such as request IDs, tenant IDs, principals, tracing metadata, or deadlines whose lifetime should match one operation.

It is not a general replacement for every `ThreadLocal`. Do not use it as mutable shared state, a resource cache, or a substitute for explicit parameters when only one or two nearby methods need the value. Ordinary executor tasks do not automatically inherit bindings; establish the binding inside the task, as this example does. Structured inheritance across child tasks is provided by `StructuredTaskScope`, which remains a preview API in Java 25.

## Official references

- [JEP 506: Scoped Values](https://openjdk.org/jeps/506)
- [Java 25 `ScopedValue` API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ScopedValue.html)
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
