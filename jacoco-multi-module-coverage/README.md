# JaCoCo multi-module coverage

This Maven reactor demonstrates why an aggregate JaCoCo report can contain zero classes even though tests ran and `jacoco.exec` files exist.

The key distinction is dependency scope. JaCoCo's `report-aggregate` goal collects class and source files from compile, runtime, and provided dependencies. Test-scoped dependencies contribute execution data only.

## Modules

- `business`: production code with no tests in the same module.
- `integration-tests`: cross-module tests that cover both customer branches and write their own execution-data file.
- `broken-report`: depends on `business` only. It sees the classes and sources but not the integration-test execution data, so the aggregate report shows 0%.
- `coverage-report`: depends on `business` with compile scope and on `integration-tests` with test scope. It combines the code with the separate module's execution data.
- `coverage-verification`: asserts that the broken CSV reports zero covered instructions and the corrected CSV reports complete `PriceCalculator` instruction and branch coverage.

## Run the reproduction and fix

Requirements: JDK 21 or newer and Maven 3.6.3 or newer.

```bash
mvn -B -ntp -f jacoco-multi-module-coverage/pom.xml clean verify
```

The build runs two tests and generates two aggregate reports:

- Broken: `broken-report/target/site/jacoco-aggregate/index.html`
- Correct: `coverage-report/target/site/jacoco-aggregate/index.html`

The corrected module uses direct reactor dependencies:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>business</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>com.example</groupId>
    <artifactId>integration-tests</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

Tested with JetBrains Runtime OpenJDK 25.0.3 using Java source target 21, Maven 3.9.16, JaCoCo 0.8.16, and JUnit 5.11.4. The example does not require Docker or an external service.
