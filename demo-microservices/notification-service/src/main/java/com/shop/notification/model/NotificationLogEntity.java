package com.shop.notification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] "Notification logs" = the data owned by this service.
// [ARCH: DATABASE-PER-SERVICE] order_id is a plain UUID (no FK). The contact
// data itself is NOT stored here; it is resolved from User Service / SMTP.
// =============================================================================
@Entity
@Table(name = "notification_logs")
@Getter
@Setter
public class NotificationLogEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
}
