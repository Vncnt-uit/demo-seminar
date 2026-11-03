package com.shop.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// =============================================================================
// [UML: Class Diagram - MICROSERVICES] class Order (boundary OrderService)
//   +Guid Id
//   +Guid CustomerId      <-- plain value, NOT an association to a Customer class
//   +decimal TotalAmount
//   +string Status
//   composition Order (1) <#>-- OrderItem (1..*)
//
// [ARCH: DATABASE-PER-SERVICE]
//   customer_id is NOT a foreign key. The "customers" table belongs to User
//   Service / Users DB and is reachable only through the User API.
//   This is the exact opposite of demo-monolith OrderEntity.customer.
// =============================================================================
@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {

    @Id
    private UUID id;

    // [UML: Class Diagram microservices] no @ManyToOne here on purpose.
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 20)
    private String status; // PENDING | CONFIRMED | FAILED | REJECTED

    // [UML: Composition] still a real composition INSIDE one service.
    // The FK lives in THIS database (order_items.order_id).
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItemEntity> items = new ArrayList<>();
}
