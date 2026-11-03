package com.shop.payment.repository;

import com.shop.payment.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] Payment Service owns Payments DB
// [UML: Sequence Diagram] used by msg 13 (process) and msg 21 (fail)
// =============================================================================
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByOrderId(UUID orderId);
}
