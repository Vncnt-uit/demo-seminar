package com.shop.order.event;

import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 15 Payment Service publishes PaymentSucceeded,
//                         msg 16 Message Broker delivers it to Order Service
//                         msg 17 Order Service -> Order DB : Update order status
// =============================================================================
public record PaymentSucceededEvent(UUID orderId, UUID paymentId) {
}
