package com.shop.order.controller;

import com.shop.order.model.dto.OrderRequestDTO;
import com.shop.order.model.dto.OrderResponseDTO;
import com.shop.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// =============================================================================
// [UML: Component Diagram]  Provided Interface of «Order Service»
// [UML: Sequence Diagram]   lifeline "Order Service", message 2 (CreateOrder)
//                           and message 6 (API Gateway -> Customer: 202 Accepted)
//
// [ARCH: MICROSERVICES] 202 Accepted, NOT 200 OK:
//   the order is only PENDING when the HTTP call returns; inventory + payment
//   continue asynchronously through the broker (Saga choreography).
// =============================================================================
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // [UML: Sequence msg 1] Customer -> API Gateway : POST /orders
    // [UML: Sequence msg 2] API Gateway -> Order Service : CreateOrder
    // [UML: Sequence msg 6] API Gateway -> Customer : 202 Accepted / "Order processing"
    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody OrderRequestDTO request) {
        return ResponseEntity.accepted().body(orderService.createOrder(request));
    }

    // Read model of the saga status: PENDING -> CONFIRMED / FAILED / REJECTED
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> get(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }
}
