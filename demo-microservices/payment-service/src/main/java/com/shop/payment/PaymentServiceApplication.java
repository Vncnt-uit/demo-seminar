package com.shop.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// =============================================================================
// [UML: Component Diagram] Component «Payment Service»
//   - Required Interface : stock.reserved  (consume)
//   - Emits              : payment.succeeded / payment.failed
//   - owns               : Payments, Transactions -> Payments DB
//   - External System    : Payment Gateway (3rd party)
//
// [UML: Deployment Diagram] Artifact = payment-service.jar
// [UML: Class Diagram] boundary "PaymentService" contains Payment
// =============================================================================
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
