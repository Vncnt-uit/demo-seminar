package com.shop.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// =============================================================================
// [UML: Component Diagram] Component «User Service»
//   - present in the Component Diagram (box + "Users DB")
//   - NOT listed in the text table "2.1 Xác định các service" -> documented gap
//   - Provided Interface : GET /api/users/{id}
//   - owns               : Users DB
//
// [UML: Class Diagram] it now owns the former CustomerEntity that the monolith
// OrderEntity used to reference with @ManyToOne.
// =============================================================================
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
