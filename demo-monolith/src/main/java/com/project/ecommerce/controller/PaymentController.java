package com.project.ecommerce.controller;

import com.project.ecommerce.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// =============================================================================
// [UML: Package Diagram] package «controller» -> class PaymentController
//   NOTE: the Package Diagram draws NO arrow from PaymentController.
//   It is still the HTTP face of PaymentService, so the class exists in the
//   folder that the Package Diagram defines. (Documented gap in MAPPING.md)
// [ARCH: MONOLITH] Same process, same database as OrderController.
// =============================================================================
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Refund is only used when the business flow needs compensation.
    // In the monolith this is a normal local transaction (no Saga needed).
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Void> refund(@PathVariable Long orderId) {
        paymentService.refund(orderId);
        return ResponseEntity.noContent().build();
    }
}
