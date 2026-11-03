package com.project.ecommerce.controller;

import com.project.ecommerce.model.dto.OrderRequestDTO;
import com.project.ecommerce.model.dto.OrderResponseDTO;
import com.project.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// =============================================================================
// [UML: Package Diagram] package «controller»  ->  class OrderController
//   Dependency (dashed, label "uses"):  OrderController ---> OrderService
// [UML: Class Diagram]  OrderController
//   +createOrder(request: OrderRequestDTO) : OrderResponseDTO
// [UML: Sequence Diagram] lifeline #2  (messages 1, 2 and 12)
// [UML: Layered flow]  REST API Controller
//
// [ARCH: MONOLITH] The controller only speaks HTTP/JSON. It never touches the DB,
// and it talks to the service by a plain Java method call (same JVM).
// =============================================================================
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // [UML: Class Diagram] the dashed "Dependency" arrow becomes a FINAL field
    // injected through the constructor (Spring DI / DIP), not `new OrderService()`.
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // [UML: Sequence msg 1] Client (SPA/Mobile) -> OrderController : POST /api/orders (OrderRequestDTO)
    // [UML: Sequence msg 2] OrderController -> OrderService : processOrder(requestDTO)
    // [UML: Sequence msg 12] OrderController -> Client : 200 OK (JSON)
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.processOrder(request);
        return ResponseEntity.ok(response); // 200 OK: everything already committed synchronously
    }
}
