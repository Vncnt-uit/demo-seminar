package com.shop.inventory.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] msg 8: Message Broker -> Inventory Service : OrderCreated
// [ARCH] Consumer-side copy of the contract. Services do NOT share an entity
// class -- only the message schema. Mirrors Order Service's OrderCreatedEvent.
// =============================================================================
public record OrderCreatedEvent(UUID orderId,
                                UUID customerId,
                                BigDecimal totalAmount,
                                List<Line> items) {

    public record Line(UUID productId, int quantity, BigDecimal unitPrice) {
    }
}
