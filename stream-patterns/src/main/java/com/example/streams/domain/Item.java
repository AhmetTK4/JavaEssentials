package com.example.streams.domain;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
public record Item(long id, String name, String category, BigDecimal price, int stock, boolean active, List<String> tags) {
 public Item {
  if (id <= 0 || name == null || name.isBlank() || category == null || category.isBlank())
   throw new IllegalArgumentException("Item identity, name and category are required");
  Objects.requireNonNull(price, "price");
  if (price.signum() < 0 || stock < 0) throw new IllegalArgumentException("Price and stock must be non-negative");
  price = price.setScale(2, java.math.RoundingMode.UNNECESSARY);
  tags = List.copyOf(tags);
  if (tags.stream().anyMatch(String::isBlank)) throw new IllegalArgumentException("Tags must not be blank");
 }
}
