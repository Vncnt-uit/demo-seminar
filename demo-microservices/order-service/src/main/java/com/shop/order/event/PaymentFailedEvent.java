package com.shop.order.event;

import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] nested alt branch [Payment failed]
//   msg 21 publish PaymentFailed
//   msg 24 deliver to Order Service
//   msg 25 Order Service -> Order DB : Mark order failed
// [ARCH] This is a business outcome, not an exception. There is no rollback here.
// =============================================================================
public record PaymentFailedEvent(UUID orderId, String reason) {
}
