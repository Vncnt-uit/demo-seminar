package com.shop.notification.repository;

import com.shop.notification.model.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] Notification Service owns Notification DB
// [UML: Sequence Diagram] msg 19 writes one log row after sending.
// =============================================================================
public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, UUID> {

    boolean existsByOrderId(UUID orderId);
}
