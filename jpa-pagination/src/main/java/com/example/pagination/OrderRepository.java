package com.example.pagination;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Query(value = """
        select o.id from PurchaseOrder o
        where o.status = :status
        order by o.createdAt desc, o.id desc
        """, countQuery = """
        select count(o) from PurchaseOrder o where o.status = :status
        """)
    Page<Long> findPageIds(@Param("status") OrderStatus status, Pageable pageable);
    @Query("""
        select o from PurchaseOrder o
        left join fetch o.items where o.id in :ids
        """)
    List<PurchaseOrder> findWithItemsByIds(@Param("ids") Collection<Long> ids);
}
