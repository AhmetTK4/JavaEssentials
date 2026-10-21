# Spring transaction self-invocation

A same-bean call bypasses proxy-mode transaction advice. This example shows two different failures: a partial JDBC write when there is no outer transaction, and an audit write that joins the outer transaction even though its method declares `REQUIRES_NEW`. The latter reports an active transaction, but still disappears on outer rollback.

## Run

Use a full JDK 21 and Maven 3.6.3 or newer, from the repository root:

```sh
mvn -B -ntp -f transactional-self-invocation/pom.xml verify
```

Verified on 2026-10-21 with Eclipse Temurin OpenJDK 21.0.12.1+1, Maven 3.9.11, Spring Boot 3.5.16, Spring Framework 6.2.19, and H2 2.3.232. Boot manages the Spring and H2 versions. Tests use H2 in memory; no Docker, credentials, external database, HTTP server, or manual schema setup is needed. `schema.sql` initializes the two tables. Tests explicitly configure auto-commit and a three-connection pool.

## Evidence

`TransactionBoundaryTest` runs seven integration tests with real Spring proxies and JDBC writes. No test-level `@Transactional` annotation is used. Each test cleans previously committed rows; `TransactionTemplate` defines explicit outer boundaries for rollback observations.

| Test | Result |
|---|---|
| `selfInvocationWithoutOuterTransactionLeavesPartialWrite` | The intentionally broken transfer throws, but leaves one ledger row |
| `externalCallToSameAnnotatedMethodRollsBack` | Calling the same annotated method through its proxy leaves no row |
| `separateBeanRollsBackFailedTransfer` | An injected `TransferWriter` rolls the failed transfer back |
| `activeTransactionDoesNotProveRequiresNewWasApplied` | Self-invoked audit reports active=true and is visible inside the outer transaction, but disappears on rollback |
| `separateBeanAuditSurvivesOuterRollback` | An injected `AuditWriter` commits the exact audit ID independently |
| `selfInvokedAuditAppearsToWorkWhenOuterTransactionCommits` | Happy-path commit hides the missed advice |
| `externalRequiresNewCallSurvivesOuterRollback` | A direct proxy call to the original method correctly applies REQUIRES_NEW |

Verified result: **7 tests, 0 failures, 0 errors, 0 skipped; Maven verify succeeded**. The reproduction tests pass by asserting defective behavior; they are not disabled or expected-failure annotations.

## Design and limits

- `UnsafeTransferService`: internal calls intentionally bypass transaction advice.
- `TransferWriter`: `REQUIRED` starts a transaction when none exists, otherwise joins it.
- `AuditWriter`: `REQUIRES_NEW` commits independently when called through its proxy.
- `SafeTransferService`: constructor-injected collaborators make the crossings explicit.
- The returned boolean is only a diagnostic probe, not a transaction identity or an application API recommendation.
- The ledger insert followed by a deliberate unchecked exception models a failed second step. It is not a banking implementation, benchmark, or production incident.
- H2 tests cover the selected imperative single-datasource semantics. They do not validate PostgreSQL/MySQL locking, distributed transactions, restart durability, reactive context, or AspectJ weaving.
- Independent audit commits can survive failed business operations. Use accurate event names and decide how audit errors should propagate.
- REQUIRES_NEW retains outer resources while acquiring another connection. Size and load-test your pool and check lock dependencies; the sequential tests and pool size of three are not production guidance.

## Primary references

Versioned official Spring Framework 6.2.19 documentation:

- [Transaction annotation and self-invocation](https://github.com/spring-projects/spring-framework/blob/v6.2.19/framework-docs/modules/ROOT/pages/data-access/transaction/declarative/annotations.adoc)
- [Propagation and connection-pool costs](https://github.com/spring-projects/spring-framework/blob/v6.2.19/framework-docs/modules/ROOT/pages/data-access/transaction/declarative/tx-propagation.adoc)
- [Proxy mechanics and alternatives](https://github.com/spring-projects/spring-framework/blob/v6.2.19/framework-docs/modules/ROOT/pages/core/aop/proxying.adoc)
- [Programmatic transactions](https://github.com/spring-projects/spring-framework/blob/v6.2.19/framework-docs/modules/ROOT/pages/data-access/transaction/programmatic.adoc)
