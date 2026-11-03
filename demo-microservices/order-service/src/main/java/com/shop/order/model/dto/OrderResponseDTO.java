package com.shop.order.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] payload of "202 Accepted / Order processing"
// [ARCH: DIFFERENCE vs monolith] status is PENDING at response time, because the
// rest of the flow (reserve -> pay -> confirm) happens asynchronously.
// =============================================================================
public record OrderResponseDTO(UUID orderId, String status, BigDecimal totalAmount, String customerName) {
}
