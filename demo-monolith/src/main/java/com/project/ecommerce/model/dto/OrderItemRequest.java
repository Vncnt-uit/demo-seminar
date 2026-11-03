package com.project.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

// =============================================================================
// [UML: Class Diagram] element type of OrderRequestDTO.items
// [ARCH: MONOLITH] Only the business fields the API client may send.
// =============================================================================
@Getter
@Setter
public class OrderItemRequest {

    private Long productId;
    private Integer quantity;
}
