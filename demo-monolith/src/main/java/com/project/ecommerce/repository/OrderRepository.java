package com.project.ecommerce.repository;

import com.project.ecommerce.model.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// =============================================================================
// [UML: Package Diagram] package «repository» -> OrderRepository
// [UML: Sequence Diagram] lifeline #6  (msgs 7 and 10: save / return OrderEntity)
// [UML: Class Diagram] "OrderService ---> OrderRepository" (uses dependency)
//
// [ARCH: MONOLITH] Spring Data JPA talks to the ONE central datasource.
// =============================================================================
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByCustomerId(Long customerId);
}
