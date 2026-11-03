package com.project.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// =============================================================================
// [UML: Class Diagram - MONOLITH] class CustomerEntity
//   -Long id
//   -String name
//   -String email
//
// [ARCH: MONOLITH] Target of the Association from OrderEntity. Same codebase,
// same database -> the object reference is legal.
// [ARCH: MICROSERVICES] This class MOVES to user-service; Order keeps only a
// UUID customerId. See demo-microservices/user-service and order-service.
// =============================================================================
@Entity
@Table(name = "customers")
@Getter
@Setter
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;
}
