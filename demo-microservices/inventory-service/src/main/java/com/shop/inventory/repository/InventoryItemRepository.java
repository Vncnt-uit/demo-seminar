package com.shop.inventory.repository;

import com.shop.inventory.model.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] Inventory Service owns Inventory DB
// [ARCH] No join to products in Order DB is possible -- the product data is HERE.
// =============================================================================
public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, UUID> {
}
