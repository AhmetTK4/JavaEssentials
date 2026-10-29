package com.example.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PriceCalculatorIntegrationTest {

    @Test
    void appliesBothDiscountsAcrossModuleBoundary() {
        PriceCalculator calculator = new PriceCalculator();

        BigDecimal standardTotal = calculator.discountedTotal(new BigDecimal("100.00"), false);
        BigDecimal premiumTotal = calculator.discountedTotal(new BigDecimal("100.00"), true);

        assertEquals(0, new BigDecimal("90.0000").compareTo(standardTotal));
        assertEquals(0, new BigDecimal("80.0000").compareTo(premiumTotal));
    }
}
