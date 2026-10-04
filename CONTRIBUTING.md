# Contributing

This repository contains focused learning examples. Keep changes tied to a documented concept and state the limitations of simplified behavior.

## Local checks

Install Maven 3.6.3 or newer and the JDK required by each example (JDK 21 for the first three commands, JDK 25 for `scoped-values`), then run from the repository root:

```sh
mvn -B -f stream-patterns/pom.xml verify
mvn -B -f transactional-events/pom.xml verify
mvn -B -f jpa-pagination/pom.xml verify
mvn -B -f scoped-values/pom.xml verify
```

Tests use in-memory examples or H2; Docker is not required. Read the example's README before changing its behavior.

## Issues and pull requests

- For bugs, include the example, Java/Maven versions, reproduction steps, and expected versus actual behavior. Remove credentials and personal data from logs.
- Discuss substantial changes in an issue first. Small documentation corrections can go straight to a pull request.
- Keep a PR focused. Explain the problem, resulting behavior, and commands you ran. Add a behavior test for a bug fix, and update examples when their public behavior changes.
- Do not describe a learning example as production-ready or claim measurements you have not made.

Maintenance is best-effort; there is no guaranteed response time.
