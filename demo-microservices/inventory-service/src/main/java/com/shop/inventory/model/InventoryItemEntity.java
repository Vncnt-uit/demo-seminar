package com.shop.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Class Diagram - MICROSERVICES] class InventoryItem (boundary InventoryService)
//   +Guid ProductId
//   +int AvailableQuantity
//
// [ARCH] unitPrice is an implementation extra needed by the "ProductId via API"
// response; the diagram only shows the two business attributes.
// =============================================================================
@Entity
@Table(name = "inventory_items")
@Getter
@Setter
public class InventoryItemEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
}
