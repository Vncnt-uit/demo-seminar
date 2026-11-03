package com.project.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// =============================================================================
// [UML: Class Diagram - MONOLITH] class OrderItemEntity (the "part" of the composition)
//   -Long id
//   -Long productId
//   -Integer quantity
//
// [ARCH: MONOLITH] productId is a plain column because ProductEntity is in the
// same database; the diagram does not draw an arrow OrderItem -> Product.
// =============================================================================
@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;
}
