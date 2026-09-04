package com.example.streams.dto;
import java.math.BigDecimal;
import com.example.streams.domain.Item;
public record ItemView(long id, String name, BigDecimal price) {
 public static ItemView from(Item item) { return new ItemView(item.id(), item.name(), item.price()); }
}
