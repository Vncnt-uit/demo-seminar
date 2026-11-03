package com.shop.payment.event;

import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 21: Payment Service -> Message Broker : PaymentFailed
//   then msg 22 -> Inventory Service (release stock)
//   and  msg 24 -> Order Service (mark failed).
// =============================================================================
public record PaymentFailedEvent(UUID orderId, String reason) {
}
