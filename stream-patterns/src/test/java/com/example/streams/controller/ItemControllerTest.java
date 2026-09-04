package com.example.streams.controller;
import com.example.streams.dto.*;
import com.example.streams.service.ItemService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(ItemController.class)
class ItemControllerTest {
 @Autowired MockMvc mvc;
 @MockitoBean ItemService service;
 final ItemView item=new ItemView(1,"Laptop",new BigDecimal("1200.00"));
 @AfterEach void interactions() { verifyNoMoreInteractions(service); }
 @Test void catalogReturnsArrayAndBindsPrice() throws Exception {
  when(service.catalog(new BigDecimal("1200.00"))).thenReturn(List.of(item));
  mvc.perform(get("/api/items/catalog").param("maxPrice","1200.00")).andExpect(status().isOk())
   .andExpect(content().contentTypeCompatibleWith("application/json"))
   .andExpect(content().json("[{\"id\":1,\"name\":\"Laptop\",\"price\":1200.00}]"));
  verify(service).catalog(new BigDecimal("1200.00"));
 }
 @Test void catalogDefaultAndEmptyArray() throws Exception {
  when(service.catalog(new BigDecimal("1000000"))).thenReturn(List.of());
  mvc.perform(get("/api/items/catalog")).andExpect(status().isOk()).andExpect(content().json("[]"));
  verify(service).catalog(new BigDecimal("1000000"));
 }
 @Test void inventoryReturnsMapWithAggregateObject() throws Exception {
  when(service.inventory()).thenReturn(Map.of("Electronics",new CategorySummary(1,3,new BigDecimal("3600.00"))));
  mvc.perform(get("/api/items/inventory")).andExpect(status().isOk())
   .andExpect(content().json("{\"Electronics\":{\"itemCount\":1,\"totalUnits\":3,\"inventoryValue\":3600.00}}")); verify(service).inventory();
 }
 @Test void availabilityReturnsObjectWithBothArrays() throws Exception {
  when(service.availability()).thenReturn(new Availability(List.of(item),List.of()));
  mvc.perform(get("/api/items/availability")).andExpect(status().isOk())
   .andExpect(jsonPath("$.available[0].id").value(1)).andExpect(jsonPath("$.unavailable").isEmpty()); verify(service).availability();
 }
 @Test void topBindsLimitAndPreservesOrder() throws Exception {
  when(service.top(2)).thenReturn(List.of(item,new ItemView(3,"Desk",new BigDecimal("300"))));
  mvc.perform(get("/api/items/top").param("limit","2")).andExpect(status().isOk())
   .andExpect(jsonPath("$[0].id").value(1)).andExpect(jsonPath("$[1].id").value(3)); verify(service).top(2);
 }
 @Test void topUsesDefaultLimit() throws Exception {
  when(service.top(3)).thenReturn(List.of()); mvc.perform(get("/api/items/top")).andExpect(status().isOk()).andExpect(content().json("[]")); verify(service).top(3);
 }
 @Test void tagsReturnsStringArray() throws Exception {
  when(service.tags()).thenReturn(List.of("a","z")); mvc.perform(get("/api/items/tags")).andExpect(status().isOk()).andExpect(content().json("[\"a\",\"z\"]")); verify(service).tags();
 }
 @ParameterizedTest @ValueSource(strings={"/top?limit=0","/top?limit=-1","/top?limit=101","/top?limit=abc","/catalog?maxPrice=-0.01","/catalog?maxPrice=abc"})
 void invalidQueriesReturn400WithoutServiceCalls(String path) throws Exception {
  mvc.perform(get("/api/items"+path)).andExpect(status().isBadRequest()); verifyNoInteractions(service);
 }
}
