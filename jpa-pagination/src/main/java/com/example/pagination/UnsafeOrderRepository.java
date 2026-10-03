package com.example.pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
/** Deliberate anti-example. With the guard enabled, calling this method fails. */
public interface UnsafeOrderRepository extends Repository<PurchaseOrder, Long> {
    @Query(value = """
        select o from PurchaseOrder o
        left join fetch o.items
        where o.status = :status
        order by o.createdAt desc, o.id desc
        """, countQuery = """
        select count(o) from PurchaseOrder o where o.status = :status
        """)
    Page<PurchaseOrder> findWithItems(@Param("status") OrderStatus status,
                                     Pageable pageable);
}
