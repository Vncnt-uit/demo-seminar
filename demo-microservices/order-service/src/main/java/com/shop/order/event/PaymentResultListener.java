package com.shop.order.event;

import com.shop.order.model.OrderEntity;
import com.shop.order.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// =============================================================================
// [UML: Sequence Diagram] the three RESULT branches of the Saga:
//   [Payment successful] msg 16/17  -> CONFIRMED
//   [Payment failed]     msg 24/25  -> FAILED  (+ Inventory releases stock)
//   [Out of stock]       msg 28/29  -> REJECTED
// [UML: Component Diagram] Required Interface of Order Service = Message Broker
//
// [ARCH: MICROSERVICES] No global transaction: each handler is a LOCAL
// transaction on order_db. Idempotency is explicit (same event may arrive twice).
// =============================================================================
@Component
public class PaymentResultListener {

    private final OrderRepository orderRepository;

    public PaymentResultListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "order.payment-succeeded", durable = "true"),
            exchange = @Exchange(name = "shop.events", type = "topic"),
            key = "payment.succeeded"))
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        updateStatus(event.orderId(), "CONFIRMED", "PENDING");
    }

    @Transactional
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "order.payment-failed", durable = "true"),
            exchange = @Exchange(name = "shop.events", type = "topic"),
            key = "payment.failed"))
    public void onPaymentFailed(PaymentFailedEvent event) {
        updateStatus(event.orderId(), "FAILED", "PENDING");
        // Inventory Service also consumes payment.failed -> releases the reservation
        // (compensating action). Order Service only changes its own row.
    }

    @Transactional
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "order.stock-rejected", durable = "true"),
            exchange = @Exchange(name = "shop.events", type = "topic"),
            key = "stock.rejected"))
    public void onStockRejected(StockRejectedEvent event) {
        updateStatus(event.orderId(), "REJECTED", "PENDING");
    }

    // Idempotency: only a PENDING order may transition, so a duplicated event
    // cannot confirm an order twice or resurrect a failed one.
    private void updateStatus(java.util.UUID orderId, String target, String expectedCurrent) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (!expectedCurrent.equals(order.getStatus())) {
            return; // already processed -- duplicate delivery
        }
        order.setStatus(target);
        // commit of THIS local transaction == message 17/25/29 on the diagram
    }
}
