package com.shop.inventory.event;

import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 22: Message Broker -> Inventory Service : PaymentFailed
// [ARCH] Triggers the COMPENSATING transaction (release stock), not a rollback.
// =============================================================================
public record PaymentFailedEvent(UUID orderId, String reason) {
}
