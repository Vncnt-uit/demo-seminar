package com.shop.inventory.event;

import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 27: Inventory Service -> Message Broker : StockRejected
//   outer alt branch [Out of stock] -> Order Service marks the order REJECTED.
// =============================================================================
public record StockRejectedEvent(UUID orderId, String reason) {
}
