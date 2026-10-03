# Java Essentials ☕

**Java fundamentals, explained through practical code.**

A growing collection of examples and small projects accompanying my Medium articles about Java. This repository connects concepts with working code, showing how Java features can solve everyday development problems.

## About

Understanding a feature is one thing; knowing when and how to use it is another.

Java Essentials brings together focused examples that you can explore, run, and modify. Some examples cover a single language concept, while others put several concepts together in a small application.

The goal is to make Java easier to understand through clear implementations, meaningful use cases, and tests where they help explain the behavior.

## Topics

The collection will grow alongside new articles, exploring topics such as:

- **Core Java** — language fundamentals, object-oriented programming, immutability, and generics
- **Collections** — choosing and using lists, sets, and maps
- **Streams and lambdas** — filtering, mapping, grouping, sorting, and aggregation
- **Exception handling** — handling failures and designing clear error flows
- **Concurrency** — threads, synchronization, and concurrent programming concepts
- **Modern Java** — records, pattern matching, and other language improvements
- **Testing** — verifying behavior with JUnit and Mockito

Some projects may use Spring Boot to demonstrate Java concepts through realistic application scenarios.

## Examples

- [JPA pagination](jpa-pagination/README.md) — Collection fetch pagination pitfalls, fail-fast configuration, ordered ID pagination, DTO mapping, and six integration tests with generated SQL checks. Java 21 and Maven; H2 without Docker.

- [Stream patterns](stream-patterns/README.md) — Five Java Stream patterns applied to an API, with explicit business rules, HTTP examples, and service/controller/integration tests. Requires Java 21 and Maven; no external services are needed.
- [Transactional events](transactional-events/README.md) — Spring’s `AFTER_COMMIT` lifecycle, the missing-audit-write problem, `REQUIRES_NEW`, atomic audit writes, and the atomic write portion of a transactional outbox. Includes complete JPA entities, repositories, commit/rollback tests, and reference PostgreSQL DDL. Requires Java 21 and Maven; tests use H2 without Docker.

## Getting Started

Clone the repository:

```bash
git clone https://github.com/AhmetTK4/JavaEssentials.git
cd JavaEssentials
```

Open the example or project you want to explore and follow its README for prerequisites, build instructions, and usage examples.

Java versions and build tools may differ between projects. Check the individual project requirements before running the code.

## Code and Articles

Published companion articles:

- [5 Practical Java Stream Patterns with Spring Boot and Java 21](https://medium.com/@ahmettemelkundupoglu/5-practical-java-stream-patterns-with-spring-boot-and-java-21-a7b6dae616b7) — [code and tests](stream-patterns/README.md)
- [Spring’s @TransactionalEventListener: Why Your Code Runs but Your Data Doesn’t Save](https://medium.com/@ahmettemelkundupoglu/springs-transactionaleventlistener-why-your-code-runs-but-your-data-doesn-t-save-7d6e84710c40) — [code and tests](transactional-events/README.md)

The articles explain the reasoning; the code gives you something to experiment with.

As the collection grows, project documentation will connect examples with their related Medium articles and describe the concepts, design choices, and expected behavior.

You can use this repository to:

- Follow along while reading an article
- Experiment with inputs and edge cases
- Compare different ways of solving a problem
- Run tests to understand the intended behavior
- Revisit a concept through a concrete example

## Approach

Examples aim to keep the main concept easy to see:

- Clear names and focused implementations
- Practical scenarios with explicit assumptions
- Tests for meaningful behavior and edge cases
- Explanations of relevant trade-offs

These are learning projects. Where an example simplifies a production concern, its documentation should make that limitation clear.

## Feedback and Contributions

Found a bug, an unclear explanation, or a useful alternative?

Feel free to open an issue or submit a pull request. Suggestions for future Java topics are welcome too.

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup, verification commands, and the expected issue/PR details. GitHub Actions verifies all examples on pull requests and pushes to `main`.

---

Written and maintained by [AhmetTK4](https://github.com/AhmetTK4).

## License

[MIT](LICENSE) — Copyright (c) 2026 Ahmet Temel Kundupoğlu.
