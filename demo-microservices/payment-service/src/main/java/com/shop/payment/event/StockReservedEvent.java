package com.shop.payment.event;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 12: Message Broker -> Payment Service : StockReserved
// [ARCH] Consumer-side copy of Inventory's contract. No shared entity classes.
// =============================================================================
public record StockReservedEvent(UUID orderId, UUID reservationId, BigDecimal amount) {
}
