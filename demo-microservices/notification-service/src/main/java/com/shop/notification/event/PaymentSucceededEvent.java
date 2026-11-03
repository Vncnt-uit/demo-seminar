package com.shop.notification.event;

import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 18: Message Broker -> Notification Service : PaymentSucceeded
// [ARCH] Consumer-side copy of the contract (no shared domain library).
// =============================================================================
public record PaymentSucceededEvent(UUID orderId, UUID paymentId) {
}
