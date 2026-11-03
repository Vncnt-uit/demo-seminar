package com.project.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// =============================================================================
// [UML: Class Diagram - MONOLITH] class OrderEntity
//   -Long id
//   -Double totalPrice
//   -String status
//   -CustomerEntity customer      (Association  many -> 1)
//   -List<OrderItemEntity> items  (Composition  1 -> many)
//
// MAPPING OF THE TWO LINES OF THE CLASS DIAGRAM:
//   Association  OrderEntity ---> CustomerEntity   => @ManyToOne + @JoinColumn("customer_id")
//   Composition  OrderEntity <#>-- OrderItemEntity => @OneToMany(cascade = ALL, orphanRemoval)
//
// [ARCH: MONOLITH] Both FKs point to tables in the SAME central database.
// This is exactly the line that DISAPPEARS in the microservices class diagram.
// =============================================================================
@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private String status;

    // [UML: Association "many" ---> "1"]  ->  DB column orders.customer_id (REAL foreign key)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_customer"))
    private CustomerEntity customer;

    // [UML: Composition "1" <#>-- "many"] -> DB FK order_items.order_id + cascade delete
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false) // unidirectional; FK lives on order_items
    private List<OrderItemEntity> items = new ArrayList<>();
}
