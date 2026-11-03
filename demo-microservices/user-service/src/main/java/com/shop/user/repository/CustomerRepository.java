package com.shop.user.repository;

import com.shop.user.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] User Service owns Users DB
// [ARCH] Only this service may query the customers table.
// =============================================================================
public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
}
