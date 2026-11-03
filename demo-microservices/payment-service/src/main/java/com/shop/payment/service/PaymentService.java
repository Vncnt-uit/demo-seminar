package com.shop.payment.service;

import com.shop.payment.gateway.PaymentGatewayClient;
import com.shop.payment.model.PaymentEntity;
import com.shop.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] lifeline "Payment Service"
//   msg 12 receive StockReserved
//   msg 13 self-call "Process payment"
//   msg 15 publish PaymentSucceeded  (nested alt [Payment successful])
//   msg 21 publish PaymentFailed     (nested alt [Payment failed])
//
// [ARCH: MICROSERVICES]
//   The local transaction below commits ONLY the payments row. If the gateway
//   succeeds but the event publish fails, Order Service never learns -> the
//   real system needs an outbox / retry here (doc 4.2).
// =============================================================================
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient gatewayClient;

    public PaymentService(PaymentRepository paymentRepository, PaymentGatewayClient gatewayClient) {
        this.paymentRepository = paymentRepository;
        this.gatewayClient = gatewayClient;
    }

    @Transactional
    public ChargeOutcome charge(UUID orderId, BigDecimal amount) {

        // Idempotency: redelivery of StockReserved must not charge twice.
        Optional<PaymentEntity> existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            PaymentEntity payment = existing.get();
            return new ChargeOutcome(payment.getId(), "SUCCEEDED".equals(payment.getStatus()),
                    payment.getStatus());
        }

        // [UML: Deployment Diagram] External System call (network!)
        PaymentGatewayClient.ChargeResult result = gatewayClient.charge(orderId, amount);

        PaymentEntity payment = new PaymentEntity();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus(result.approved() ? "SUCCEEDED" : "FAILED");
        paymentRepository.save(payment);

        return new ChargeOutcome(payment.getId(), result.approved(), result.reference());
    }

    // [UML: Compensating transaction] money is not "rolled back": a refund is a
    // NEW transaction, then the status changes to REFUNDED.
    @Transactional
    public void refund(UUID orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (!"SUCCEEDED".equals(payment.getStatus())) {
                return;
            }
            gatewayClient.refund(orderId);
            payment.setStatus("REFUNDED");
        });
    }

    public record ChargeOutcome(UUID paymentId, boolean approved, String reason) {
    }
}
