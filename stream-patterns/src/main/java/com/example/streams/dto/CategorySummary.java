package com.example.streams.dto;
import java.math.BigDecimal;
public record CategorySummary(long itemCount, long totalUnits, BigDecimal inventoryValue) {}
