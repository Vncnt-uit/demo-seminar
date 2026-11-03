package com.project.ecommerce.model.dto;

import lombok.Getter;
import lombok.Setter;

// =============================================================================
// [UML: Class Diagram] input of PaymentService.processPayment(paymentInfo)
// [UML: Sequence Diagram] msg 5 argument
// =============================================================================
@Getter
@Setter
public class PaymentInfo {

    private String method;   // CARD / WALLET / COD
    private Double amount;
    private String token;    // tokenized card, never the raw card number
}
