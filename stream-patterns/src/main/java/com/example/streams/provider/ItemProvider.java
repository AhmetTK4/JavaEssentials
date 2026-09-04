package com.example.streams.provider;
import com.example.streams.domain.Item;
import java.util.List;
/** Non-null snapshot; no null elements; each product ID occurs once. */
public interface ItemProvider { List<Item> findAll(); }
