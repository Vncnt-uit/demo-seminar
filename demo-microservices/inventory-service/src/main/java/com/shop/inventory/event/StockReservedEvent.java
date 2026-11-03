package com.shop.inventory.event;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 11: Inventory Service -> Message Broker : StockReserved
// [UML: Component Diagram] Producer side of the async flow
// [ARCH] Carries the order amount so Payment Service can charge without
// querying Order DB (which it must not access).
// =============================================================================
public record StockReservedEvent(UUID orderId, UUID reservationId, BigDecimal amount) {
}
