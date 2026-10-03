# JPA pagination without collection-fetch surprises

Companion example for **Your Spring Boot Endpoint Returns 20 Orders. Why Is Hibernate Loading Thousands?**

Java 21, Maven, Spring Boot 3.5.13, and its managed Hibernate 6.6 dependency. H2 is test-only; Docker is not required. This is a focused repository/service example, not an HTTP server.

## Run

```sh
cd jpa-pagination
mvn test
```

## Code map

- [UnsafeOrderRepository](src/main/java/com/example/pagination/UnsafeOrderRepository.java): deliberate collection-fetch + Pageable anti-example.
- [OrderRepository](src/main/java/com/example/pagination/OrderRepository.java): paginate ordered parent IDs, then fetch their collections without pagination.
- [OrderQueryService](src/main/java/com/example/pagination/OrderQueryService.java): validate page size, restore ID order, preserve totals on empty pages, and map inside a transaction.
- [PurchaseOrder](src/main/java/com/example/pagination/PurchaseOrder.java) and [OrderItem](src/main/java/com/example/pagination/OrderItem.java): complete entity mappings.
- [OrderView](src/main/java/com/example/pagination/OrderView.java) and [ItemView](src/main/java/com/example/pagination/ItemView.java): detached response records.
- [application.properties](src/main/resources/application.properties): fail fast on collection-fetch pagination.
- [PaginationTest](src/test/java/com/example/pagination/PaginationTest.java): six integration tests, including generated SQL inspection through [SqlCapture](src/test/java/com/example/pagination/SqlCapture.java).

The fixture has 25 matching orders sharing a timestamp, three items each, and one excluded order. The first full page uses three statements: ID page, count, and collection fetch. Tests check the actual H2 limit syntax, complete collections, deterministic order, second/empty pages, and invalid sizes. SQL assertions are intentionally dialect-specific; they are not PostgreSQL performance measurements.

## Limits and design choices

- `readOnly=true` does not guarantee a consistent snapshot between queries. The service fails explicitly if a selected parent disappears; choose a retry/isolation policy for a real API.
- Paging parents does not bound child cardinality. Use a summary projection when the screen needs counts rather than full items.
- A `Page` can issue a count query. A `Slice` can avoid total-count metadata, but does not itself fix collection fetching.
- Other eager associations can introduce extra SQL. The example keeps the child-to-parent association lazy.
- Hibernate behavior depends on version and dialect. This example targets 6.6, not every future version.
- No benchmark, concurrent-delete integration test, PostgreSQL execution, REST controller, or production indexing strategy is included.

## References

- [Hibernate 6.6 HQL: limits and offsets](https://docs.hibernate.org/orm/6.6/querylanguage/html_single/#limits)
- [Hibernate QuerySettings: pagination guard](https://docs.hibernate.org/stable/orm/javadocs/org/hibernate/cfg/QuerySettings.html#FAIL_ON_PAGINATION_OVER_COLLECTION_FETCH)
- [Spring Data: paging and query methods](https://docs.spring.io/spring-data/commons/reference/repositories/query-methods-details.html#repositories.special-parameters)
- [PostgreSQL: LIMIT and OFFSET](https://www.postgresql.org/docs/current/queries-limit.html)
- [Hibernate maintainers: EntityGraph and pagination](https://discourse.hibernate.org/t/hibernate-orm-6-entitygraph-and-pagination-search-warning/8942)
