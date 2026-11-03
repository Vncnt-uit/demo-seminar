package com.project.ecommerce.service;

import com.project.ecommerce.model.dto.PaymentInfo;
import com.project.ecommerce.model.entity.PaymentEntity;
import com.project.ecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;

// =============================================================================
// [UML: Package Diagram] package «service» -> class PaymentService
// [UML: Sequence Diagram] lifeline #5
//   msg 5: processPayment(paymentInfo)
//   msg 6: return "Payment Success"
// [UML: Deployment Diagram] External System = Payment Gateway (3rd party)
//
// [ARCH: MONOLITH]
//   The payment ROW is written into the same central database, in the caller's
//   transaction. There is no Payment DB of its own and no PaymentFailed event.
// =============================================================================
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient gatewayClient;

    public PaymentService(PaymentRepository paymentRepository, PaymentGatewayClient gatewayClient) {
        this.paymentRepository = paymentRepository;
        this.gatewayClient = gatewayClient;
    }

    // [UML: Sequence msg 5] in-process call
    public PaymentEntity processPayment(PaymentInfo info) {
        boolean approved = gatewayClient.charge(info); // external system, but the DB write is local

        if (!approved) {
            // Rollback of the whole order transaction -> stock is restored automatically.
            throw new IllegalStateException("Payment declined");
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setAmount(info.getAmount());
        payment.setStatus("SUCCESS");
        // orderId is completed by OrderService after the order row is inserted.
        return paymentRepository.save(payment);
    }

    // Compensation is a simple local transaction in the monolith.
    public void refund(Long orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.setStatus("REFUNDED");
            gatewayClient.refund(orderId);
        });
    }
}
