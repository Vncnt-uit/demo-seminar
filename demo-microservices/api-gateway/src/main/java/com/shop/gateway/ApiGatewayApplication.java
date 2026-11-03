package com.shop.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// =============================================================================
// [UML: Component Diagram] Component «API Gateway»
//   - the single entry point: Client -> API Gateway -> Order Service
//   - owns NO business data ("Không sở hữu dữ liệu nghiệp vụ")
//   - routes only the PUBLIC interfaces; inventory/payment/notification are
//     internal and intentionally not exposed.
//
// [UML: Deployment Diagram] Artifact = api-gateway.jar
//   In the Kubernetes diagram this role is played by Load Balancer + API Gateway.
// =============================================================================
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
