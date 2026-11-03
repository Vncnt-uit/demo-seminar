package com.project.ecommerce.service;

import com.project.ecommerce.model.dto.PaymentInfo;
import org.springframework.stereotype.Component;

// =============================================================================
// [UML: Deployment Diagram] External System = "Payment Gateway" (VNPay/MoMo/Stripe)
// [UML: Sequence Diagram] not a lifeline: the diagram stops at PaymentService.
//
// Mock only: no real HTTP call. It exists to show that the payment provider is
// OUTSIDE the monolith's database boundary even in the MVC version.
// =============================================================================
@Component
public class PaymentGatewayClient {

    public boolean charge(PaymentInfo info) {
        // POST https://gateway.example.com/charge  (mock: always approves)
        return true;
    }

    public void refund(Long orderId) {
        // POST https://gateway.example.com/refund
    }
}
