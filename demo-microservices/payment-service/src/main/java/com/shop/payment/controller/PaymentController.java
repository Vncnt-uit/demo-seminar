package com.shop.payment.controller;

import com.shop.payment.model.PaymentEntity;
import com.shop.payment.repository.PaymentRepository;
import com.shop.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// =============================================================================
// [UML: Component Diagram] Provided Interface of «Payment Service».
//   NOTE: the API Gateway does NOT route here (payment is internal), which is
//   why this controller is only reachable inside the service network.
// [UML: Sequence Diagram] not a lifeline: messages 12..15 are broker-driven.
// =============================================================================
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentController(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentEntity> get(@PathVariable UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Compensating endpoint: "Nếu đã thu tiền thì cần hoàn tiền" (doc 4.3).
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Void> refund(@PathVariable UUID orderId) {
        paymentService.refund(orderId);
        return ResponseEntity.noContent().build();
    }
}
