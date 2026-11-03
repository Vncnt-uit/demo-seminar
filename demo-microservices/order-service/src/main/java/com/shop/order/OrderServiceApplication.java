package com.shop.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// =============================================================================
// [UML: Component Diagram]  Component «Order Service»
//   - inside the "Services" boundary, behind the API Gateway
//   - Provided Interface : POST /api/orders, GET /api/orders/{id}
//   - Required Interface : Inventory API (ProductId via API), User API,
//                          Message Broker (publish OrderCreated,
//                          consume PaymentSucceeded / PaymentFailed / StockRejected)
//   - owns                    : Orders DB
//
// [UML: Deployment Diagram] Artifact = order-service.jar -> container "order-service"
// [UML: Class Diagram] boundary "OrderService" contains Order + OrderItem
//
// [ARCH: MICROSERVICES] ONE bounded context = ONE process = ONE database.
//   Inside it we STILL use Controller -> Service -> Repository (MVC in a service).
//   Compare with demo-monolith: same layering, different process/data boundary.
// =============================================================================
@SpringBootApplication
@EnableScheduling // only used if you add the Transactional Outbox relay
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
