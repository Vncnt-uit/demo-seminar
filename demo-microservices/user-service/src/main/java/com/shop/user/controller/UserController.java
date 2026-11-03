package com.shop.user.controller;

import com.shop.user.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] Provided Interface of «User Service».
// [ARCH: DIFFERENCE] In the monolith, OrderEntity had a real CustomerEntity
// object (association). Here the ONLY way Order Service can read a customer is
// this HTTP endpoint, because Users DB is a separate owner.
// =============================================================================
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CustomerRepository customerRepository;

    public UserController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerView> get(@PathVariable UUID id) {
        return customerRepository.findById(id)
                .map(c -> ResponseEntity.ok(new CustomerView(c.getId(), c.getName(), c.getEmail())))
                .orElse(ResponseEntity.notFound().build());
    }

    public record CustomerView(UUID id, String name, String email) {
    }
}
