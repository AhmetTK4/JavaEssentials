# 5 Java Stream Patterns for Real-World Business Scenarios

Java 21 • Spring Boot 3.5.13 • Maven • JUnit 5 • Mockito

A runnable learning project built to accompany a Medium article. The article itself is not included. “Most commonly used” is not a statistical ranking: these five patterns demonstrate common application needs and different Stream tools.

## Running the Application

Install JDK **21** and Maven **3.6.3+**. Make sure `JAVA_HOME` points to JDK 21; check with `mvn -version`.

```sh
mvn clean verify
mvn spring-boot:run
# Alternative:
java -jar target/stream-patterns-1.0.0.jar
```

Server: `http://localhost:8080`. No external services, database, or credentials are required.

## Architecture

```text
HTTP → ItemController → ItemService → ItemProvider
                                        └─ FakeItemProvider → immutable Item records
                      └─ ItemView / CategorySummary / Availability DTOs → JSON
```

The controller binds and validates HTTP parameters. The service contains the business rules and Stream operations. The provider defines a replaceable data-source contract and can be implemented by a database adapter in a production application. The fake source supplies data at runtime; Mockito mocks are used only in tests. Dependencies use constructor injection.

## Five Scenarios

| Endpoint | Pattern | Business Need | JSON |
|---|---|---|---|
| GET `/api/items/catalog?maxPrice=300` | filter + map | Select purchasable products within a budget and map them to DTOs | array |
| GET `/api/items/inventory` | groupingBy + collectingAndThen + sum/reduce | Calculate inventory value by category | map of objects |
| GET `/api/items/availability` | partitioningBy + mapping | Split active products by stock availability | object with arrays |
| GET `/api/items/top?limit=3` | sorted + limit | Return the N most expensive purchasable products | array |
| GET `/api/items/tags` | flatMap + distinct + sorted | Build unique filter options from nested tags | string array |

`partitioningBy` was chosen over `toMap`: category grouping already demonstrates a map result, while partitioning demonstrates collecting items into two business groups in one collection pass. `reduce` is included in category valuation. Money is summed using `BigDecimal` rather than double-based summary statistics.

## Data and Business Rules

All prices use a single currency: USD. `inventoryValue` is the sum of unit price × stock for each product; it is not sales revenue. Monetary values have two decimal places, fractional cents are rejected, and no rounding is performed. Stock totals use `long` to avoid overflowing an `int` total.

| ID | Product | Category | Price | Stock | Active |
|---|---|---|---:|---:|---|
| 1 | Laptop | Electronics | 1200.00 | 3 | yes |
| 2 | Mouse | Electronics | 25.50 | 0 | yes |
| 3 | Desk | Furniture | 300.00 | 5 | yes |
| 4 | Chair | Furniture | 150.00 | 2 | no |
| 5 | Monitor | Electronics | 300.00 | 4 | yes |
| 6 | Notebook | Stationery | 5.00 | 10 | yes |

All endpoints exclude inactive products. Catalog and top also exclude products with zero stock; tags include those from active products with zero stock. Catalog and availability use ascending ID order. Top uses descending price order, then ascending ID for equal prices. Tags are deduplicated case-sensitively and returned in natural String order. Category keys are sorted using TreeMap; clients should not depend on JSON object key order.

The provider returns a non-null snapshot with no null elements and unique product IDs. Repeated categories and tags are valid: duplicate tags are removed, while different products in the same category remain part of the totals. Repeated product IDs violate the provider contract; the service does not silently merge them. The domain's tag lists and the fake provider's data list cannot be modified externally.

## Request and Response Examples

All five read operations use GET; they have no request body. Filters are query parameters, and response bodies are JSON.

### 1. Catalog Within a Budget

```sh
curl -s 'http://localhost:8080/api/items/catalog?maxPrice=300'
```

```json
[
  {"id":3,"name":"Desk","price":300.00},
  {"id":5,"name":"Monitor","price":300.00},
  {"id":6,"name":"Notebook","price":5.00}
]
```

`maxPrice` is an inclusive upper bound; minimum 0, default 1000000. Products with a zero price are valid.

### 2. Inventory Summary by Category

```sh
curl -s 'http://localhost:8080/api/items/inventory'
```

```json
{
  "Electronics":{"itemCount":3,"totalUnits":7,"inventoryValue":4800.00},
  "Furniture":{"itemCount":1,"totalUnits":5,"inventoryValue":1500.00},
  "Stationery":{"itemCount":1,"totalUnits":10,"inventoryValue":50.00}
}
```

`itemCount` counts active products, not stock units. Mouse is counted despite having zero stock. The inactive Chair is excluded.

### 3. Partition by Availability

```sh
curl -s 'http://localhost:8080/api/items/availability'
```

```json
{
  "available":[
    {"id":1,"name":"Laptop","price":1200.00},
    {"id":3,"name":"Desk","price":300.00},
    {"id":5,"name":"Monitor","price":300.00},
    {"id":6,"name":"Notebook","price":5.00}
  ],
  "unavailable":[{"id":2,"name":"Mouse","price":25.50}]
}
```

### 4. Top N Most Expensive Products

```sh
curl -s 'http://localhost:8080/api/items/top?limit=3'
```

```json
[
  {"id":1,"name":"Laptop","price":1200.00},
  {"id":3,"name":"Desk","price":300.00},
  {"id":5,"name":"Monitor","price":300.00}
]
```

`limit`: 1–100, default 3. A limit larger than the number of eligible products returns all eligible products.

### 5. Unique Tags

```sh
curl -s 'http://localhost:8080/api/items/tags'
```

```json
["accessory","display","home","portable","work"]
```

The two occurrences of `display` within Monitor and the repeated `work` tags across products each appear once. Notebook's empty tag list is handled normally.

### Empty Results and Errors

If the source is empty or contains only inactive products, catalog/top/tags return `[]`, inventory returns `{}`, and availability returns `{"available":[],"unavailable":[]}`, all with HTTP 200.

```sh
curl -i 'http://localhost:8080/api/items/top?limit=0'
curl -i 'http://localhost:8080/api/items/catalog?maxPrice=-1'
curl -i 'http://localhost:8080/api/items/top?limit=abc'
```

Invalid values or types produce HTTP 400. Spring MVC Problem Details is enabled; error-message wording depends on the Spring version. Provider failures are not converted into successful empty results.

## Testing Approach

- **Service:** Mockito isolates the provider. Tests cover boundary prices, free products, inactive/zero-stock products, decimal precision, repeated categories/tags, ordering ties, stock totals exceeding the int range, empty input, and provider failures.
- **Interactions:** The provider is called once per successful service call and never for invalid service arguments. Unexpected additional interactions are rejected.
- **Controller:** `@WebMvcTest`, `MockMvc`, and `@MockitoBean` verify JSON shapes, parameter binding, defaults, ordering, and HTTP 400 responses. Invalid HTTP input does not invoke the service.
- **Integration:** A real server starts on a random port to test the HTTP → controller → service → fake provider chain.
- **Domain:** Tests verify defensive copying, negative values, and fractional-cent behavior.

Test reports: `target/surefire-reports/`. See [VERIFICATION.md](VERIFICATION.md) for the recorded verification results.

## Design Limitations

This is a small in-memory learning dataset. For large datasets, move filtering, sorting, top-N selection, and suitable aggregations to the database and introduce pagination. Sorting typically costs O(n log n); filtering, mapping, and flattening are generally linear in the number of elements processed. The category collector creates intermediate lists for teaching clarity. There is no measured need for parallel streams. Authentication, data writes, and persistent storage are outside the scope of this example.

## Official References

- [Spring Boot 3.5 System Requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Java 21 Stream API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html)
- [Java 21 Collectors](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Collectors.html)
