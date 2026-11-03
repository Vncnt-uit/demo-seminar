package com.shop.order.event;

import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] outer alt branch [Out of stock]
//   msg 27 publish StockRejected
//   msg 28 deliver to Order Service
//   msg 29 Order Service -> Order DB : Mark order rejected
// =============================================================================
public record StockRejectedEvent(UUID orderId, String reason) {
}
