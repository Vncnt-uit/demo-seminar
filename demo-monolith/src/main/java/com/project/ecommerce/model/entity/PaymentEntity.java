package com.project.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// =============================================================================
// [UML: Package Diagram] package «model.entity» -> PaymentEntity
//   (listed in the folder tree; the Part-2 Class Diagram focuses on Order only)
// [ARCH: MONOLITH] Same central DB => payments.order_id can be a real FK.
// [ARCH: MICROSERVICES] Becomes payment-service/model/PaymentEntity in Payment DB,
// where order_id is only an identifier (no FK).
// =============================================================================
@Entity
@Table(name = "payments")
@Getter
@Setter
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status;
}
