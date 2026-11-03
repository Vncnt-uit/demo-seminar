package com.shop.inventory.repository;

import com.shop.inventory.model.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] used by msg 9 (reserve), msg 27 (reject),
//                         msg 23 (release, compensating action)
// =============================================================================
public interface StockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {

    Optional<StockReservationEntity> findByOrderId(UUID orderId);
}
