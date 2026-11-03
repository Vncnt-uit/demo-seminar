package com.project.ecommerce.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// =============================================================================
// [UML: Package Diagram] package «config» (Infrastructure Layer)
// [UML: Sequence Diagram] the "Exception" return path of the transaction frame:
//   any RuntimeException inside @Transactional -> rollback -> this handler -> 400
//
// [ARCH: MONOLITH] ONE advice for ALL layers because they share one process.
// [ARCH: MICROSERVICES] Each service needs its own handler; cross-service errors
// arrive as events/timeouts, not as Java exceptions.
// =============================================================================
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException ex) {
        // Mirrors the snippet in "PHẦN 3 - Bước 3" of the seminar document.
        return ResponseEntity.badRequest().body("Order Failed: " + ex.getMessage());
    }
}
