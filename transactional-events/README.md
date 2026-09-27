# Transactional events: after commit, independent writes, and the outbox

Companion code for **Spring’s @TransactionalEventListener: Why Your Code Runs but Your Data Doesn’t Save**.

This standalone Spring Boot example uses Java 21 and JPA. Tests use an in-memory H2 database, so Docker and PostgreSQL are not required. The Boot version matches the existing `stream-patterns` project.

## Run the examples

With JDK 21 and Maven 3.6.3 or newer installed, run from the repository root:

```sh
mvn -f transactional-events/pom.xml test
```

Or open `transactional-events/pom.xml` as a Maven project in your IDE and run the test classes. The examples are exercised through tests; there is no HTTP endpoint or standalone server to start.

## Where to look

- [OrderService.java](src/main/java/com/example/events/OrderService.java): three alternative workflows. `placeOrder()` publishes an event; `placeOrderWithAtomicAudit()` writes the order and audit together; `placeOrderWithOutbox()` writes the order and pending event together. Only the first workflow publishes `OrderPlaced`, so the alternatives do not trigger a duplicate audit.
- [OrderPlaced.java](src/main/java/com/example/events/OrderPlaced.java): immutable event carrying the order ID.
- [OrderAuditListener.java](src/main/java/com/example/events/OrderAuditListener.java): synchronous listener using the default `AFTER_COMMIT` phase.
- [AuditWriter.java](src/main/java/com/example/events/AuditWriter.java): a separate bean with `REQUIRES_NEW`, invoked through Spring’s transaction proxy.
- [Order.java](src/main/java/com/example/events/Order.java), [AuditEntry.java](src/main/java/com/example/events/AuditEntry.java), and [OutboxEvent.java](src/main/java/com/example/events/OutboxEvent.java): complete JPA mappings and constructors omitted from the article snippets. `purchase_order` avoids the SQL keyword `order`.
- [OrderRepository.java](src/main/java/com/example/events/OrderRepository.java), [AuditRepository.java](src/main/java/com/example/events/AuditRepository.java), and [OutboxRepository.java](src/main/java/com/example/events/OutboxRepository.java): Spring Data repositories sharing one transaction manager and datasource.
- [MissingAuditTest.java](src/test/java/com/example/events/MissingAuditTest.java): the deliberately broken listener. The test checks that `save()` returns and the order commits, while the audit row is absent. The broken listener is confined to this test context.
- [OrderTransactionsTest.java](src/test/java/com/example/events/OrderTransactionsTest.java): checks the corrected listener, rollback, publication without a transaction, atomic audit writes, and atomic outbox writes.
- [schema-postgresql.sql](schema-postgresql.sql): reference PostgreSQL DDL. Tests generate their H2 schema from the entity mappings; they do not execute this script.
- [application.properties](src/test/resources/application.properties): test-only database settings.

## What the tests demonstrate

The tests intentionally have no test-level `@Transactional` annotation. They use `TransactionTemplate` when a real commit or rollback boundary is required, then read the result outside that transaction. Committed rows are cleaned up after each test.

1. A listener can call `save()` after commit without committing the audit row.
2. Calling the separate `AuditWriter` bean gives the corrected listener a new transaction. The audit is absent before commit and present after transaction completion.
3. Rolling back the publishing transaction skips the after-commit listener.
4. Publishing without a transaction skips the listener with its default configuration.
5. The atomic-audit workflow commits both records, or rolls back both.
6. The outbox workflow commits a pending event with the order, or rolls back both.

## Limits of the example

`REQUIRES_NEW` makes the audit write independent. An audit failure cannot undo the already committed order. It can also require another connection while the original transaction’s resources remain bound; size and test the pool for your workload.

The outbox example implements only the atomic database write. It does not include a polling relay, broker, email sender, retry scheduler, or consumer deduplication. `publishedAt` remains null because nothing delivers these events yet. The event ID can support deduplication in a future consumer; it does not provide exactly-once delivery by itself.

H2 is in-memory and loses data when the process exits. These tests verify transaction behavior, **not restart durability**. A real outbox needs persistent storage, a relay with retries and concurrency handling, idempotent consumers, and monitoring of pending events. The PostgreSQL DDL is a reference, not a tested PostgreSQL deployment or migration setup.

## References

- [Spring: transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html)
- [Spring: TransactionalEventListener contract](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/event/TransactionalEventListener.html)
- [Spring: transaction propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)
- [Spring: transactional testing](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html)
- [Transactional outbox pattern](https://microservices.io/patterns/data/transactional-outbox.html)

The published article link will be added once the article is available.
