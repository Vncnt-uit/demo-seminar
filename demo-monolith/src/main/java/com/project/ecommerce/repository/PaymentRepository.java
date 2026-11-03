package com.project.ecommerce.repository;

import com.project.ecommerce.model.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// =============================================================================
// [UML: Package Diagram] package «repository» -> PaymentRepository
// [ARCH: MONOLITH] payments lives in the same schema as orders and products.
// =============================================================================
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);
}
