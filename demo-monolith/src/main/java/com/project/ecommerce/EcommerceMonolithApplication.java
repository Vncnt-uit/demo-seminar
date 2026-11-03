package com.project.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// =============================================================================
// [UML: Deployment Diagram]  Artifact = ecommerce-monolith.jar (ONE deployable)
// [UML: Package Diagram]     Root package com.project.ecommerce
//                            -> controller / service / repository / model / config
//
// [ARCH: MONOLITH]
//   All 7 lifelines of the Part-2 Sequence Diagram
//   (Client, OrderController, OrderService, InventoryService, PaymentService,
//    OrderRepository, Database) live inside THIS single JVM.
//   => calls are in-process, the database is one central schema.
//   The microservices twin is demo-microservices/order-service, inventory-service, ...
// =============================================================================
@SpringBootApplication
public class EcommerceMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceMonolithApplication.class, args);
    }
}
