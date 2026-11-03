package com.project.ecommerce.service;

import com.project.ecommerce.model.dto.OrderItemRequest;
import com.project.ecommerce.model.entity.ProductEntity;
import com.project.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// =============================================================================
// [UML: Package Diagram] package «service» -> class InventoryService
// [UML: Sequence Diagram] lifeline #4
//   msg 3: checkAndReserveStock(items)
//   msg 4: return "Stock Reserved"
//
// [ARCH: MONOLITH]
//   "Reserve" is just a local UPDATE on the shared products table, inside the
//   caller's transaction. If a later step fails, the SAME transaction rolls the
//   quantity back -- no compensating action is needed.
//   Microservices twin: inventory-service (own DB + explicit release/refund).
// =============================================================================
@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // [UML: Sequence msg 3] -- called directly by OrderService, no network
    public void checkAndReserveStock(List<OrderItemRequest> items) {
        for (OrderItemRequest line : items) {
            // Row lock on the CENTRAL database; all services share this table.
            ProductEntity product = productRepository.findByIdForUpdate(line.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.getProductId()));

            if (product.getStockQuantity() < line.getQuantity()) {
                // Any exception rolls back the whole @Transactional frame.
                throw new IllegalStateException("Out of stock: " + product.getId());
            }
            product.setStockQuantity(product.getStockQuantity() - line.getQuantity());
            // JPA dirty checking -> UPDATE products ... inside the same transaction.
        }
    }
}
