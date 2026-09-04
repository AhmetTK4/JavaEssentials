package com.example.streams.controller;
import com.example.streams.dto.*;
import com.example.streams.service.ItemService;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
@RestController
@RequestMapping("/api/items")
public class ItemController {
 private final ItemService service;
 public ItemController(ItemService service) { this.service = service; }
 @GetMapping("/catalog")
 public List<ItemView> catalog(@RequestParam(defaultValue="1000000") @DecimalMin("0.00") BigDecimal maxPrice) { return service.catalog(maxPrice); }
 @GetMapping("/inventory") public Map<String, CategorySummary> inventory() { return service.inventory(); }
 @GetMapping("/availability") public Availability availability() { return service.availability(); }
 @GetMapping("/top") public List<ItemView> top(@RequestParam(defaultValue="3") @Min(1) @Max(100) int limit) { return service.top(limit); }
 @GetMapping("/tags") public List<String> tags() { return service.tags(); }
}
