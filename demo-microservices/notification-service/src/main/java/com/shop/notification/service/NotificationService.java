package com.shop.notification.service;

import com.shop.notification.channel.EmailSender;
import com.shop.notification.model.NotificationLogEntity;
import com.shop.notification.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] lifeline "Notification Service"
//   msg 18 broker -> Notification : PaymentSucceeded
//   msg 19 self-call "Send confirmation"
//
// [ARCH: MICROSERVICES] Fully asynchronous and decoupled: Order Service does not
// know this service exists. Adding a new consumer does not redeploy the others.
// =============================================================================
@Service
public class NotificationService {

    private final EmailSender emailSender;
    private final NotificationLogRepository logRepository;

    public NotificationService(EmailSender emailSender, NotificationLogRepository logRepository) {
        this.emailSender = emailSender;
        this.logRepository = logRepository;
    }

    // [UML: Sequence msg 19]
    @Transactional
    public void sendConfirmation(UUID orderId) {

        // Idempotency: broker delivers at-least-once, so guard with the log table.
        if (logRepository.existsByOrderId(orderId)) {
            return;
        }

        emailSender.sendOrderConfirmation(orderId); // External System: SMTP / email provider

        NotificationLogEntity log = new NotificationLogEntity();
        log.setId(UUID.randomUUID());
        log.setOrderId(orderId);
        log.setChannel("EMAIL");
        log.setStatus("SENT");
        log.setSentAt(Instant.now());
        logRepository.save(log);
    }
}
