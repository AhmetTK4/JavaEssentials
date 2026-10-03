package com.example.pagination;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test")
class PaginationTest {
    @Autowired OrderRepository orders;
    @Autowired UnsafeOrderRepository unsafe;
    @Autowired OrderQueryService service;
    private final List<Long> expectedIds = new ArrayList<>();
    @BeforeEach void seed() {
        orders.deleteAll();
        Instant timestamp = Instant.parse("2026-01-01T12:00:00Z");
        for (int i = 0; i < 25; i++) {
            PurchaseOrder order = new PurchaseOrder(timestamp, OrderStatus.OPEN);
            for (int j = 0; j < 3; j++) order.addItem("SKU-" + j, j + 1);
            expectedIds.add(orders.saveAndFlush(order).getId());
        }
        orders.saveAndFlush(new PurchaseOrder(timestamp.plusSeconds(60), OrderStatus.CLOSED));
        Collections.reverse(expectedIds);
        SqlCapture.clear();
    }
    @AfterEach void clearCapture() { SqlCapture.clear(); }
    @Test void rejectsCollectionFetchPagination() {
        assertThatThrownBy(() -> unsafe.findWithItems(OrderStatus.OPEN, PageRequest.of(0, 20)))
            .hasStackTraceContaining("fail_on_pagination_over_collection_fetch");
    }
    @Test void returnsOrderedPageWithCompleteCollectionsAndDatabaseLimit() {
        var page = service.find(OrderStatus.OPEN, 0, 20);
        assertThat(page.getContent()).extracting(OrderView::id)
            .containsExactlyElementsOf(expectedIds.subList(0, 20));
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getContent()).allSatisfy(order -> {
            assertThat(order.items()).extracting(ItemView::sku)
                .containsExactly("SKU-0", "SKU-1", "SKU-2");
        });
        List<String> sql = SqlCapture.statements();
        assertThat(sql).hasSize(3); // ID page, count, and collection fetch for this full page.
        assertThat(sql).anySatisfy(statement -> {
            assertThat(statement).contains("purchase_orders", "fetch first");
            assertThat(statement).doesNotContain("join");
        });
        assertThat(sql).anySatisfy(statement -> {
            assertThat(statement).contains("join order_items", " in (");
            assertThat(statement).doesNotContain("fetch first", "offset");
        });
    }
    @Test void secondPageHasNoOverlapAndRetainsCompleteItems() {
        var page = service.find(OrderStatus.OPEN, 1, 20);
        assertThat(page.getContent()).extracting(OrderView::id)
            .containsExactlyElementsOf(expectedIds.subList(20, 25));
        assertThat(page.getContent()).allSatisfy(order -> assertThat(order.items()).hasSize(3));
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.hasNext()).isFalse();
    }
    @Test void outOfRangePageKeepsTotalAndSkipsCollectionFetch() {
        var page = service.find(OrderStatus.OPEN, 2, 20);
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(SqlCapture.statements()).noneMatch(sql -> sql.contains("join order_items"));
    }
    @Test void emptyDatasetReturnsZeroTotal() {
        orders.deleteAll();
        assertThat(service.find(OrderStatus.OPEN, 0, 20).getTotalElements()).isZero();
    }
    @Test void rejectsUnboundedOrInvalidPageSizes() {
        assertThatIllegalArgumentException().isThrownBy(() -> service.find(OrderStatus.OPEN, -1, 20));
        assertThatIllegalArgumentException().isThrownBy(() -> service.find(OrderStatus.OPEN, 0, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> service.find(OrderStatus.OPEN, 0, 101));
    }
}
