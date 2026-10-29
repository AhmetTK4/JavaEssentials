package com.example.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoverageReportTest {

    @Test
    void directCodeDependencyTurnsAnEmptyAggregateIntoACompleteReport() throws IOException {
        Path reactorRoot = Path.of("..").toAbsolutePath().normalize();
        Path brokenCsv = reactorRoot.resolve("broken-report/target/site/jacoco-aggregate/jacoco.csv");
        Path correctCsv = reactorRoot.resolve("coverage-report/target/site/jacoco-aggregate/jacoco.csv");

        assertTrue(Files.exists(brokenCsv), "The broken aggregate report should still be generated");
        String brokenCalculator = Files.readAllLines(brokenCsv).stream()
                .filter(line -> line.contains(",PriceCalculator,"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("PriceCalculator is missing from the broken report"));
        String[] brokenCounters = brokenCalculator.split(",");
        assertEquals("0", brokenCounters[4], "No instructions should be covered without test execution data");
        assertEquals("0", brokenCounters[6], "No branches should be covered without test execution data");

        List<String> correctLines = Files.readAllLines(correctCsv);
        String priceCalculator = correctLines.stream()
                .filter(line -> line.contains(",PriceCalculator,"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("PriceCalculator is missing from the aggregate report"));

        String[] counters = priceCalculator.split(",");
        assertEquals("0", counters[3], "No instructions should be missed");
        assertEquals("0", counters[5], "No branches should be missed");
    }
}
