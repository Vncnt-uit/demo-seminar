package com.shop.inventory.controller;

import com.shop.inventory.repository.InventoryItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Component Diagram]  Provided Interface of «Inventory Service»
// [UML: Class Diagram]      the dashed Order Service ---> Inventory Service
//                           dependency labelled "ProductId via API"
// [UML: Sequence Diagram]   NOT drawn as a lifeline: the order saga reaches
//                           Inventory only through the broker. This API exists
//                           for the synchronous price/stock lookup at order time.
//
// [ARCH] A network call now replaces the monolith's direct ProductEntity read.
// =============================================================================
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryItemRepository itemRepository;

    public InventoryController(InventoryItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryView> get(@PathVariable UUID productId) {
        return itemRepository.findById(productId)
                .map(item -> ResponseEntity.ok(
                        new InventoryView(item.getProductId(), item.getAvailableQuantity(), item.getUnitPrice())))
                .orElse(ResponseEntity.notFound().build());
    }

    public record InventoryView(UUID productId, int availableQuantity, BigDecimal unitPrice) {
    }
}
