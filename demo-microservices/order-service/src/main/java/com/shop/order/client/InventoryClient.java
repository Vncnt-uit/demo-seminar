package com.shop.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Class Diagram - MICROSERVICES] the DASHED DEPENDENCY
//   Order Service ---- "ProductId via API" ----> Inventory Service
// [UML: Component Diagram] "Dependency giua component" = HTTP client
//
// [ARCH: MICROSERVICES] <-- the single biggest change vs the monolith:
//   a UML association/attribute read becomes a NETWORK call that can timeout,
//   return 5xx, or be slow. The monolith read `ProductEntity` from RAM.
// =============================================================================
@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Value("${services.inventory.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    // Sync REST. A real system adds timeout + retry + circuit breaker here
    // (Resilience4j) and fails fast if Inventory Service is unavailable.
    public ProductSnapshot getProduct(UUID productId) {
        return restClient.get()
                .uri("/api/inventory/{productId}", productId)
                .retrieve()
                .body(ProductSnapshot.class);
    }

    // Local copy of the fields this service needs -- NOT the Inventory entity.
    public record ProductSnapshot(UUID productId, BigDecimal unitPrice, int availableQuantity) {
    }
}
