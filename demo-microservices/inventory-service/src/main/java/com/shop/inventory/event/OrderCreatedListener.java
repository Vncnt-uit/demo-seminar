package com.shop.inventory.event;

import com.shop.inventory.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// =============================================================================
// [UML: Sequence Diagram]
//   outer alt [Stock available]:
//     msg 8  broker -> Inventory : OrderCreated
//     msg 9  self-call Check and reserve stock
//     msg 11 publish StockReserved
//   outer alt [Out of stock]:
//     msg 27 publish StockRejected
//
// [ARCH] The listener is the "Required Interface" of Inventory Service.
// Queue + exchange + routing key are visible in the @QueueBinding below --
// this is the code form of the "Async Flow" arrow on the Component Diagram.
// =============================================================================
@Component
public class OrderCreatedListener {

    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;

    public OrderCreatedListener(InventoryService inventoryService, RabbitTemplate rabbitTemplate) {
        this.inventoryService = inventoryService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "inventory.order-created", durable = "true"),
            exchange = @Exchange(name = "shop.events", type = "topic"),
            key = "order.created"))
    public void onOrderCreated(OrderCreatedEvent event) {
        // reserve() commits its OWN local transaction and returns the outcome.
        InventoryService.ReservationResult result = inventoryService.reserve(event);

        if (result.reserved()) {
            // [UML: Sequence msg 11]
            rabbitTemplate.convertAndSend("shop.events", "stock.reserved",
                    new StockReservedEvent(event.orderId(), result.reservationId(), event.totalAmount()));
        } else {
            // [UML: Sequence msg 27]
            rabbitTemplate.convertAndSend("shop.events", "stock.rejected",
                    new StockRejectedEvent(event.orderId(), result.reason()));
        }
    }
}
