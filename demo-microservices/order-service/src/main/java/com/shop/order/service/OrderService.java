package com.shop.order.service;

import com.shop.order.client.InventoryClient;
import com.shop.order.client.UserClient;
import com.shop.order.event.OrderCreatedEvent;
import com.shop.order.event.OrderEventPublisher;
import com.shop.order.model.OrderEntity;
import com.shop.order.model.OrderItemEntity;
import com.shop.order.model.dto.OrderRequestDTO;
import com.shop.order.model.dto.OrderResponseDTO;
import com.shop.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// =============================================================================
// [UML: Class Diagram - MICROSERVICES] class Order inside boundary OrderService
//   +Guid Id
//   +Guid CustomerId        <-- VALUE ONLY, no Customer object, no FK
//   +decimal TotalAmount
//   +string Status
//   (composition with OrderItem is still allowed: same service, same DB)
//
// [UML: Sequence Diagram] lifeline "Order Service":
//   msg 3  Save Pending Order
//   msg 5  Publish OrderCreated
//   msg 16/24/28  receive PaymentSucceeded / PaymentFailed / StockRejected
//
// [ARCH: MICROSERVICES - THE KEY DIFFERENCE]
//   @Transactional below covers ONLY order_db. Committing here does NOT commit
//   inventory or payment. There is NO distributed rollback -> Saga + compensation.
// =============================================================================
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;   // [UML] dependency "ProductId via API"
    private final UserClient userClient;             // [UML] replaces OrderEntity.customer
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        InventoryClient inventoryClient,
                        UserClient userClient,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.userClient = userClient;
        this.eventPublisher = eventPublisher;
    }

    // [UML: Sequence msg 3] "Save Pending Order" -- LOCAL transaction only.
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        UUID orderId = UUID.randomUUID();
        List<OrderItemEntity> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequestDTO.Item item : request.items()) {
            // [UML: Class Diagram microservices] dashed arrow Order Service -> Inventory Service
            // labelled "ProductId via API". This is a NETWORK call, not a field access.
            // [ARCH] Timeout/retry/circuit breaker belong here (see MAPPING.md).
            InventoryClient.ProductSnapshot product = inventoryClient.getProduct(item.productId());

            OrderItemEntity line = new OrderItemEntity();
            line.setId(UUID.randomUUID());
            line.setOrderId(orderId);
            line.setProductId(product.productId());
            line.setQuantity(item.quantity());
            // Price SNAPSHOT: the microservice order keeps the price of the moment,
            // because a later price change in Inventory Service must not rewrite history.
            line.setUnitPrice(product.unitPrice());
            lines.add(line);

            total = total.add(product.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        // [UML: Class Diagram microservices] Order only stores CustomerId.
        // No @ManyToOne, no JOIN to a customers table (that table is in Users DB).
        order.setCustomerId(request.customerId());
        order.setStatus("PENDING");
        order.setTotalAmount(total);
        order.setItems(lines);

        orderRepository.save(order);

        // [UML: Sequence msg 5] Publish OrderCreated to the Message Broker.
        // [ARCH] In production wrap this in a Transactional Outbox so that
        // "row committed" and "event published" cannot diverge (doc 4.2).
        eventPublisher.publishOrderCreated(OrderCreatedEvent.from(order));

        // Customer name is NOT in Order DB: it is read from User Service over HTTP.
        String customerName = userClient.getCustomerName(order.getCustomerId());

        // [UML: Sequence msg 6] controller returns 202 Accepted with PENDING status
        return new OrderResponseDTO(order.getId(), order.getStatus(), total, customerName);
    }

    public OrderResponseDTO getOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        String customerName = userClient.getCustomerName(order.getCustomerId());
        return new OrderResponseDTO(order.getId(), order.getStatus(), order.getTotalAmount(), customerName);
    }
}
