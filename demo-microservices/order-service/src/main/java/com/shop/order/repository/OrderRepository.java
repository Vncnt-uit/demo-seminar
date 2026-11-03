package com.shop.order.repository;

import com.shop.order.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] Order Service owns the "Orders DB"
// [UML: Sequence Diagram] messages 3/4 (save), 17 (update status), 25/29 (mark failed/rejected)
// [ARCH] This repository can ONLY see order_db. It cannot join customers or payments.
// =============================================================================
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
}
