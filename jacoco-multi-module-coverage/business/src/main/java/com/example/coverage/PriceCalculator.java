package com.example.coverage;

import java.math.BigDecimal;

public final class PriceCalculator {

    private static final BigDecimal STANDARD_DISCOUNT = new BigDecimal("0.10");
    private static final BigDecimal PREMIUM_DISCOUNT = new BigDecimal("0.20");

    public BigDecimal discountedTotal(BigDecimal subtotal, boolean premiumCustomer) {
        BigDecimal discount = premiumCustomer ? PREMIUM_DISCOUNT : STANDARD_DISCOUNT;
        return subtotal.subtract(subtotal.multiply(discount));
    }
}
