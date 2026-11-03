package com.project.ecommerce.service;

import com.project.ecommerce.model.entity.OrderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// =============================================================================
// [UML: Package Diagram] package «service» -> class NotificationService
// [UML: Package Diagram] present in the package, but NOT drawn as a lifeline in
//                        the Part-2 Sequence Diagram (extends beyond msg 12).
//
// [ARCH: MONOLITH] Called as a normal method inside the order transaction/thread.
// The microservices version is an ASYNC consumer of PaymentSucceeded:
//   demo-microservices/notification-service/.../PaymentSucceededListener.java
// =============================================================================
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendOrderConfirmation(OrderEntity order) {
        // In-process: no queue, no retry, no separate Notification DB.
        log.info("[MONOLITH] Email confirmation for order {} to {}",
                order.getId(), order.getCustomer().getEmail());
    }
}
