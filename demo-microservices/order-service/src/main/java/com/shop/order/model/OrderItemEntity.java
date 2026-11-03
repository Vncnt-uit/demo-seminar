package com.shop.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Class Diagram - MICROSERVICES] class OrderItem
//   +Guid Id
//   +Guid OrderId
//   +Guid ProductId
//   +int Quantity
//   +decimal UnitPrice      <-- present ONLY in the microservices class diagram
//
// [ARCH: DIFFERENCE] UnitPrice is a price snapshot: the order history must not
// change when Inventory Service updates the product price.
// =============================================================================
@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItemEntity {

    @Id
    private UUID id;

    // Part of the local composition. The actual FK is order_items.order_id.
    @Column(name = "order_id", nullable = false, insertable = false, updatable = false)
    private UUID orderId;

    // [ARCH] Plain identifier of a product owned by Inventory Service. No FK.
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
}
