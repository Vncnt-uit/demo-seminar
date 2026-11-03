package com.shop.order.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// =============================================================================
// [UML: Sequence Diagram] msg 5: Order Service -> Message Broker : Publish OrderCreated
// [UML: Component Diagram] the "Async Flow" arrow to the Message Queue
//
// [ARCH] Only the Order Service publishes order.created; it never calls
// Inventory or Payment directly for the saga (choreography, doc 4.2).
// =============================================================================
@Component
public class OrderEventPublisher {

    public static final String EXCHANGE = "shop.events";
    public static final String ORDER_CREATED_KEY = "order.created";

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(EXCHANGE, ORDER_CREATED_KEY, event);
    }
}
