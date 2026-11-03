package com.shop.payment.event;

import com.shop.payment.service.PaymentService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// =============================================================================
// [UML: Sequence Diagram]
//   msg 12 broker -> Payment Service : StockReserved
//   msg 13 self-call Process payment
//   nested alt [Payment successful] -> msg 15 publish PaymentSucceeded
//   nested alt [Payment failed]     -> msg 21 publish PaymentFailed
//
// [ARCH] This listener is the Required Interface of Payment Service.
// It reacts to an event; nobody "calls" it from Order Service.
// =============================================================================
@Component
public class StockReservedListener {

    private final PaymentService paymentService;
    private final RabbitTemplate rabbitTemplate;

    public StockReservedListener(PaymentService paymentService, RabbitTemplate rabbitTemplate) {
        this.paymentService = paymentService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "payment.stock-reserved", durable = "true"),
            exchange = @Exchange(name = "shop.events", type = "topic"),
            key = "stock.reserved"))
    public void onStockReserved(StockReservedEvent event) {

        // charge() commits its own local transaction on Payments DB.
        PaymentService.ChargeOutcome outcome = paymentService.charge(event.orderId(), event.amount());

        if (outcome.approved()) {
            // [UML: Sequence msg 15]
            rabbitTemplate.convertAndSend("shop.events", "payment.succeeded",
                    new PaymentSucceededEvent(event.orderId(), outcome.paymentId()));
        } else {
            // [UML: Sequence msg 21]
            rabbitTemplate.convertAndSend("shop.events", "payment.failed",
                    new PaymentFailedEvent(event.orderId(), outcome.reason()));
        }
    }
}
