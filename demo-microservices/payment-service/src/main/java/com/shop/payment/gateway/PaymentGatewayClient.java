package com.shop.payment.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Deployment Diagram] External System = Payment Gateway (VNPay/MoMo/Stripe)
// [UML: Sequence Diagram] msg 13 "Process payment" leaves the system boundary.
//
// [ARCH: MICROSERVICES] A network failure here is NORMAL (doc section 7), so it
// is mapped to a business failure (payment.failed) instead of an exception that
// would try to "roll back" other services.
// =============================================================================
@Component
public class PaymentGatewayClient {

    private final RestClient restClient;

    public PaymentGatewayClient(@Value("${payments.gateway.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ChargeResult charge(UUID orderId, BigDecimal amount) {
        try {
            GatewayResponse response = restClient.post()
                    .uri("/charge")
                    .body(new ChargeRequest(orderId.toString(), amount))
                    .retrieve()
                    .body(GatewayResponse.class);
            return new ChargeResult(response != null && response.approved(),
                    response == null ? "empty-response" : response.reference());
        } catch (RestClientException ex) {
            // timeout / 5xx / connection refused -> business failure, not a DB rollback
            return new ChargeResult(false, "gateway-unavailable");
        }
    }

    public void refund(UUID orderId) {
        try {
            restClient.post().uri("/refund").body(new RefundRequest(orderId.toString())).retrieve().toBodilessEntity();
        } catch (RestClientException ignored) {
            // A real system would retry / queue this compensation.
        }
    }

    public record ChargeRequest(String orderId, BigDecimal amount) {
    }

    public record RefundRequest(String orderId) {
    }

    public record GatewayResponse(boolean approved, String reference) {
    }

    public record ChargeResult(boolean approved, String reference) {
    }
}
