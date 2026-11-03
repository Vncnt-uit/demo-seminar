package com.project.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// =============================================================================
// [UML: Package Diagram] package «model.entity» -> ProductEntity
//   (listed in the folder tree, but not drawn on the Part-2 Class Diagram)
// [ARCH: MONOLITH] Lives in the central DB and is read directly by
// InventoryService and ProductRepository.
// [ARCH: MICROSERVICES] Moves to inventory-service (InventoryItem / products).
// =============================================================================
@Entity
@Table(name = "products")
@Getter
@Setter
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
}
