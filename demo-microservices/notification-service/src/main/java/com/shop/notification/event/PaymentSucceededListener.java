package com.shop.notification.event;

import com.shop.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// =============================================================================
// [UML: Sequence Diagram]
//   msg 18 Message Broker -> Notification Service : PaymentSucceeded
//   msg 19 self-call Send confirmation
//
// [ARCH: MICROSERVICES] This is the ONLY entry point of the service. There is no
// controller, which matches the Component Diagram: Notification hangs off the
// Message Queue, not off the API Gateway.
// =============================================================================
@Component
public class PaymentSucceededListener {

    private final NotificationService notificationService;

    public PaymentSucceededListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "notification.payment-succeeded", durable = "true"),
            exchange = @Exchange(name = "shop.events", type = "topic"),
            key = "payment.succeeded"))
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        notificationService.sendConfirmation(event.orderId());
    }
}
