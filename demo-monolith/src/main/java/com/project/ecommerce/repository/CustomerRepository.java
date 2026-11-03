package com.project.ecommerce.repository;

import com.project.ecommerce.model.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// =============================================================================
// [UML: Class Diagram - MONOLITH] implied by the Association
//   OrderEntity "many" ---> CustomerEntity "1"
//   (The Package Diagram lists only 3 repositories; this 4th one is required to
//    navigate the association -- documented gap in MAPPING.md)
// [ARCH: MICROSERVICES] Replaced by user-service + HTTP call (UserClient).
// =============================================================================
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
}
