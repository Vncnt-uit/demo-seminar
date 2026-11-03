package com.shop.payment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Class Diagram - MICROSERVICES] class Payment (boundary PaymentService)
//   +Guid Id
//   +Guid OrderId
//   +decimal Amount
//   +string Status
//
// [ARCH: DATABASE-PER-SERVICE] order_id is a PLAIN UUID, unique for
// idempotency. There is NO foreign key to the Orders DB.
// Payment Service owns this data; Order Service may only ask for it via API.
// =============================================================================
@Entity
@Table(name = "payments")
@Getter
@Setter
public class PaymentEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String status; // SUCCEEDED | FAILED | REFUNDED
}
