package com.example.streams.service;
import com.example.streams.domain.Item;
import com.example.streams.provider.ItemProvider;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
 @Mock ItemProvider provider;
 @InjectMocks ItemService service;
 static Item item(long id, String category, String price, int stock, boolean active, String... tags) {
  return new Item(id, "Item " + id, category, new BigDecimal(price), stock, active, List.of(tags));
 }
 void given(Item... items) { when(provider.findAll()).thenReturn(List.of(items)); }
 @AfterEach void interactions() { verifyNoMoreInteractions(provider); }
 void readOnce() { verify(provider).findAll(); }
 @Test void catalogFiltersAndMapsInclusiveBoundaryInIdOrder() {
  given(item(3,"A","10",1,true),item(1,"A","10.00",1,true),item(2,"A","1",0,true),item(4,"A","1",1,false),item(5,"A","10.01",1,true));
  var result=service.catalog(new BigDecimal("10"));
  assertThat(result).extracting(v->v.id()).containsExactly(1L,3L);
  assertThat(result.getFirst().name()).isEqualTo("Item 1");
  assertThat(result.getFirst().price()).isEqualByComparingTo("10.00"); readOnce();
 }
 @Test void catalogAllowsFreeProducts() {
  given(item(1,"A","0",1,true)); assertThat(service.catalog(BigDecimal.ZERO)).hasSize(1); readOnce();
 }
 @Test void inventoryGroupsRepeatedCategoriesAndUsesExactDecimalArithmetic() {
  given(item(1,"Z","0.10",3,true),item(2,"Z","0.20",2,true),item(3,"A","9",0,true),item(4,"Z","100",9,false));
  var result=service.inventory();
  assertThat(result.keySet()).containsExactly("A","Z");
  assertThat(result.get("Z").itemCount()).isEqualTo(2);
  assertThat(result.get("Z").totalUnits()).isEqualTo(5);
  assertThat(result.get("Z").inventoryValue()).isEqualByComparingTo("0.70");
  assertThat(result.get("A").inventoryValue()).isEqualByComparingTo("0"); readOnce();
 }
 @Test void totalUnitsDoesNotOverflowInt() {
  given(item(1,"A","1",Integer.MAX_VALUE,true),item(2,"A","1",Integer.MAX_VALUE,true));
  assertThat(service.inventory().get("A").totalUnits()).isEqualTo(4294967294L); readOnce();
 }
 @Test void availabilityPartitionsActiveItemsInIdOrder() {
  given(item(3,"A","1",1,true),item(2,"A","1",0,true),item(1,"A","1",2,true),item(4,"A","1",0,false));
  var result=service.availability();
  assertThat(result.available()).extracting(v->v.id()).containsExactly(1L,3L);
  assertThat(result.unavailable()).extracting(v->v.id()).containsExactly(2L); readOnce();
 }
 @Test void availabilityAlwaysIncludesBothPartitions() {
  given(item(1,"A","1",0,true)); var result=service.availability();
  assertThat(result.available()).isEmpty(); assertThat(result.unavailable()).hasSize(1); readOnce();
 }
 @Test void topBreaksEqualPricesByIdAndFiltersBeforeLimiting() {
  given(item(4,"A","999",0,true),item(5,"A","999",1,false),item(3,"A","10",1,true),item(1,"A","10",1,true),item(2,"A","5",1,true));
  assertThat(service.top(2)).extracting(v->v.id()).containsExactly(1L,3L); readOnce();
 }
 @Test void topLimitLargerThanInputReturnsAll() {
  given(item(1,"A","1",1,true)); assertThat(service.top(100)).hasSize(1); readOnce();
 }
 @Test void tagsFlattensDeduplicatesWithinAndAcrossItemsAndSorts() {
  given(item(1,"A","1",0,true,"z","a","z"),item(2,"A","1",1,true,"a","b"),item(3,"A","1",1,false,"hidden"),item(4,"A","1",1,true));
  assertThat(service.tags()).containsExactly("a","b","z"); readOnce();
 }
 @ParameterizedTest @ValueSource(strings={"empty","inactive"})
 void allPatternsHandleNoEligibleItems(String scenario) {
  given(scenario.equals("empty") ? new Item[]{} : new Item[]{item(1,"A","1",1,false)});
  assertThat(service.catalog(BigDecimal.TEN)).isEmpty(); assertThat(service.inventory()).isEmpty();
  var availability=service.availability(); assertThat(availability.available()).isEmpty(); assertThat(availability.unavailable()).isEmpty();
  assertThat(service.top(1)).isEmpty(); assertThat(service.tags()).isEmpty(); verify(provider,times(5)).findAll();
 }
 @ParameterizedTest @ValueSource(ints={-1,0,101}) void rejectsInvalidLimitWithoutReading(int limit) {
  assertThatIllegalArgumentException().isThrownBy(()->service.top(limit)); verifyNoInteractions(provider);
 }
 @Test void rejectsInvalidPriceWithoutReading() {
  assertThatIllegalArgumentException().isThrownBy(()->service.catalog(new BigDecimal("-1")));
  assertThatIllegalArgumentException().isThrownBy(()->service.catalog(null)); verifyNoInteractions(provider);
 }
 @Test void propagatesProviderFailureInsteadOfReturningMisleadingEmptyResult() {
  when(provider.findAll()).thenThrow(new IllegalStateException("offline"));
  assertThatIllegalStateException().isThrownBy(()->service.tags()).withMessage("offline"); readOnce();
 }
}
