package com.shop.order.event;

import com.shop.order.model.OrderEntity;
import com.shop.order.model.OrderItemEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] message 5: Order Service -> Message Broker
//                         "Publish OrderCreated"
// [UML: Component Diagram] "Required Interface" of Inventory Service =
//                         the message it consumes
// [ARCH] Event contract, NOT a shared entity. Each service has its own copy
// of the class (see inventory-service/event/OrderCreatedEvent.java).
// =============================================================================
public record OrderCreatedEvent(UUID orderId,
                                UUID customerId,
                                BigDecimal totalAmount,
                                List<Line> items) {

    public static OrderCreatedEvent from(OrderEntity order) {
        List<Line> lines = order.getItems().stream()
                .map(OrderCreatedEvent::lineOf)
                .toList();
        return new OrderCreatedEvent(order.getId(), order.getCustomerId(),
                order.getTotalAmount(), lines);
    }

    private static Line lineOf(OrderItemEntity item) {
        return new Line(item.getProductId(), item.getQuantity(), item.getUnitPrice());
    }

    public record Line(UUID productId, int quantity, BigDecimal unitPrice) {
    }
}
