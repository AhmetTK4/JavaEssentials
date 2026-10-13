# Spring `UnexpectedRollbackException`

This example reproduces a transaction that is marked rollback-only after a database constraint violation, even though the application catches the exception.

## The trap

`UnsafePublishingService` catches `DataIntegrityViolationException` inside its `@Transactional` method and tries to write an audit row. The database error has already made the physical transaction unusable. When the method returns normally, Spring reaches the outer transaction boundary and throws `UnexpectedRollbackException` instead of pretending the work committed.

## The safe boundary

`SafePublishingFacade` is not transactional. It calls `PublishingAttempt`, whose transaction either commits or rolls back completely. Only after that transaction has ended does the facade catch the exception and call `FailureAuditService` in a separate transaction.

The example intentionally demonstrates transaction boundaries, not a complete publishing system. A production system may prefer an outbox, durable queue, or centralized audit pipeline depending on its delivery requirements.

## Run

Requirements: Java 21 and Maven 3.6.3 or newer.

```bash
mvn -B -ntp verify
```

The integration tests prove both behaviors:

- Catching the database exception inside the doomed transaction still produces `UnexpectedRollbackException`, and the inline audit row rolls back.
- Catching outside the failed transaction allows a separate audit transaction to commit and returns an explicit rejected result.

## Relevant files

- `UnsafePublishingService.java` — the caught-exception trap
- `SafePublishingFacade.java` — orchestration outside the failed transaction
- `PublishingAttempt.java` — the transaction that may fail
- `FailureAuditService.java` — the independent audit transaction
- `PublishingTransactionTest.java` — executable proof
