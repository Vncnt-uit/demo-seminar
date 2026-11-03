package com.shop.payment.event;

import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 15: Payment Service -> Message Broker : PaymentSucceeded
//   then msg 16 -> Order Service (update status) and msg 18 -> Notification Service.
// =============================================================================
public record PaymentSucceededEvent(UUID orderId, UUID paymentId) {
}
