package com.project.ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// =============================================================================
// [UML: Class Diagram] OrderResponseDTO is the OUTPUT of the order flow
// [UML: Sequence Diagram] msg 11 (OrderService -> OrderController) and
//                         msg 12 (Controller -> Client, serialized as JSON)
// [ARCH: MONOLITH] Flattened projection: orderId + totalPrice + customerName.
// It is NOT OrderEntity: no lazy proxies, no circular Order -> Customer -> Order.
// =============================================================================
@Getter
@AllArgsConstructor
public class OrderResponseDTO {

    private Long orderId;
    private Double totalPrice;
    private String customerName;
}
