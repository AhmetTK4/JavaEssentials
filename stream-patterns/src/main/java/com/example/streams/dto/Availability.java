package com.example.streams.dto;
import java.util.List;
public record Availability(List<ItemView> available, List<ItemView> unavailable) {}
