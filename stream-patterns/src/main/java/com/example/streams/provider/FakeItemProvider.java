package com.example.streams.provider;
import com.example.streams.domain.Item;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Repository;
@Repository
public class FakeItemProvider implements ItemProvider {
 private final List<Item> items = List.of(
  new Item(1, "Laptop", "Electronics", new BigDecimal("1200.00"), 3, true, List.of("work", "portable")),
  new Item(2, "Mouse", "Electronics", new BigDecimal("25.50"), 0, true, List.of("work", "accessory")),
  new Item(3, "Desk", "Furniture", new BigDecimal("300.00"), 5, true, List.of("work", "home")),
  new Item(4, "Chair", "Furniture", new BigDecimal("150.00"), 2, false, List.of("home")),
  new Item(5, "Monitor", "Electronics", new BigDecimal("300.00"), 4, true, List.of("work", "display", "display")),
  new Item(6, "Notebook", "Stationery", new BigDecimal("5.00"), 10, true, List.of()));
 public List<Item> findAll() { return items; }
}
