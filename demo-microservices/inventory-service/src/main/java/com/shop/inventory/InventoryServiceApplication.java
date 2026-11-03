package com.shop.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// =============================================================================
// [UML: Component Diagram] Component «Inventory Service»
//   - Provided Interface : GET /api/inventory/{productId}  (used by Order Service)
//   - Required Interface : order.created (consume), payment.failed (consume)
//   - owns                : Products, Inventory, Reservations -> Inventory DB
//
// [UML: Deployment Diagram] Artifact = inventory-service.jar
// [UML: Class Diagram] boundary "InventoryService" contains
//                      InventoryItem + StockReservation
// =============================================================================
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
