package com.example.streams.service;
import com.example.streams.domain.Item;
import com.example.streams.dto.*;
import com.example.streams.provider.ItemProvider;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import static java.util.stream.Collectors.*;
@Service
public class ItemService {
 private final ItemProvider provider;
 public ItemService(ItemProvider provider) { this.provider = provider; }
 public List<ItemView> catalog(BigDecimal maxPrice) {
  if (maxPrice == null || maxPrice.signum() < 0) throw new IllegalArgumentException("maxPrice must be non-negative");
  return provider.findAll().stream()
   .filter(i -> i.active() && i.stock() > 0 && i.price().compareTo(maxPrice) <= 0)
   .sorted(Comparator.comparingLong(Item::id)).map(ItemView::from).toList();
 }
 public Map<String, CategorySummary> inventory() {
  return provider.findAll().stream().filter(Item::active)
   .collect(groupingBy(Item::category, TreeMap::new, collectingAndThen(toList(), items ->
    new CategorySummary(items.size(), items.stream().mapToLong(Item::stock).sum(),
     items.stream().map(i -> i.price().multiply(BigDecimal.valueOf(i.stock())))
      .reduce(new BigDecimal("0.00"), BigDecimal::add)))));
 }
 public Availability availability() {
  var partitions = provider.findAll().stream().filter(Item::active)
   .sorted(Comparator.comparingLong(Item::id))
   .collect(partitioningBy(i -> i.stock() > 0, mapping(ItemView::from, toList())));
  return new Availability(partitions.get(true), partitions.get(false));
 }
 public List<ItemView> top(int limit) {
  if (limit < 1 || limit > 100) throw new IllegalArgumentException("limit must be between 1 and 100");
  return provider.findAll().stream().filter(i -> i.active() && i.stock() > 0)
   .sorted(Comparator.comparing(Item::price).reversed().thenComparingLong(Item::id))
   .limit(limit).map(ItemView::from).toList();
 }
 public List<String> tags() {
  return provider.findAll().stream().filter(Item::active)
   .flatMap(i -> i.tags().stream()).distinct().sorted().toList();
 }
}
