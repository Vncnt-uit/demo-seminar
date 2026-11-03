package com.shop.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] dependency Order Service -> User Service.
//   The Component Diagram shows a separate "Users DB" and no shared schema, so
//   customer data can only arrive through this provided interface.
//
// [ARCH: DIFFERENCE] In the monolith this was `order.getCustomer().getName()`
// (an object navigation inside one JVM). Here it is an HTTP request.
// The alternative to the sync call is a local read-model kept in sync by events.
// =============================================================================
@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(@Value("${services.user.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String getCustomerName(UUID customerId) {
        CustomerSnapshot customer = restClient.get()
                .uri("/api/users/{id}", customerId)
                .retrieve()
                .body(CustomerSnapshot.class);
        return customer == null ? "unknown" : customer.name();
    }

    public record CustomerSnapshot(UUID id, String name, String email) {
    }
}
