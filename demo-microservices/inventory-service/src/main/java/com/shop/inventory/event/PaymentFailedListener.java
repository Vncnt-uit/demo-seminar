package com.shop.inventory.event;

import com.shop.inventory.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// =============================================================================
// [UML: Sequence Diagram] nested alt [Payment failed]:
//   msg 22 broker -> Inventory : PaymentFailed
//   msg 23 self-call Release reserved stock
//
// [ARCH: SAGA COMPENSATION] This is the code that replaces a distributed
// ROLLBACK. The stock UPDATE of msg 9 was already committed; adding the
// quantity back is a brand-new local transaction.
// =============================================================================
@Component
public class PaymentFailedListener {

    private final InventoryService inventoryService;

    public PaymentFailedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "inventory.payment-failed", durable = "true"),
            exchange = @Exchange(name = "shop.events", type = "topic"),
            key = "payment.failed"))
    public void onPaymentFailed(PaymentFailedEvent event) {
        inventoryService.release(event.orderId());
    }
}
