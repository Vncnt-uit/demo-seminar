package com.shop.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// =============================================================================
// [UML: Component Diagram] Component «Notification Service»
//   - Required Interface : payment.succeeded (consume)
//   - owns               : Notification logs -> Notification DB
//   - draws NO HTTP controller: on the Component Diagram it is attached to the
//     Message Queue, not to the API Gateway.
//
// [UML: Deployment Diagram] Artifact = notification-service.jar
// =============================================================================
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
