package com.example.streams.domain;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
class ItemTest {
 @Test void defensivelyCopiesTags() {
  var tags=new ArrayList<>(List.of("work")); var item=new Item(1,"a","b",BigDecimal.ONE,0,true,tags);
  tags.add("new"); assertThat(item.tags()).containsExactly("work");
  assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(()->item.tags().add("x"));
 }
 @Test void rejectsNegativeStockAndPriceAndFractionalCents() {
  assertThatIllegalArgumentException().isThrownBy(()->new Item(1,"a","b",BigDecimal.ONE,-1,true,List.of()));
  assertThatIllegalArgumentException().isThrownBy(()->new Item(1,"a","b",new BigDecimal("-1"),0,true,List.of()));
  assertThatExceptionOfType(ArithmeticException.class).isThrownBy(()->new Item(1,"a","b",new BigDecimal("0.001"),0,true,List.of()));
 }
}
