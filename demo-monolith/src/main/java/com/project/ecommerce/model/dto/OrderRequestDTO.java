package com.project.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// =============================================================================
// [UML: Class Diagram] OrderRequestDTO is the INPUT type of
//   OrderController.createOrder(...) and OrderService.processOrder(...)
// [UML: Package Diagram] package «model.dto»
// [ARCH: MONOLITH] The DTO is deliberately FLAT: it never exposes OrderEntity
// (that would leak DB structure and can cause circular JSON).
// =============================================================================
@Getter
@Setter
public class OrderRequestDTO {

    private Long customerId;
    private List<OrderItemRequest> items;
    private PaymentInfo paymentInfo;
}
