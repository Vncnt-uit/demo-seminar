package com.shop.user.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

// =============================================================================
// [UML: Class Diagram - MONOLITH] class CustomerEntity
//   -Long id / -String name / -String email
// [ARCH: MICROSERVICES] The SAME business concept, moved into its own bounded
// context. Order Service keeps only the UUID, never this object.
// =============================================================================
@Entity
@Table(name = "customers")
@Getter
@Setter
public class CustomerEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;
}
