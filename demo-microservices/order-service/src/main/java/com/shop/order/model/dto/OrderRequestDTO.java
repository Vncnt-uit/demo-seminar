package com.shop.order.model.dto;

import java.util.List;
import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] part of the Provided Interface: POST /api/orders
// [ARCH] The client sends only identifiers + quantities. Product price and
// customer name are resolved by the Order Service through other services.
// =============================================================================
public record OrderRequestDTO(UUID customerId, List<Item> items) {

    public record Item(UUID productId, int quantity) {
    }
}
