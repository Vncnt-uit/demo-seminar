package com.shop.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// =============================================================================
// [UML: Class Diagram - MICROSERVICES] class StockReservation (boundary InventoryService)
//   +Guid ReservationId
//   +Guid OrderId
//   +string Status
//
// [ARCH] orderId is a PLAIN identifier from Order Service (no FK to Orders DB).
// The reserved lines are an implementation detail required to release stock
// later (compensating transaction); the conceptual diagram omits them.
// =============================================================================
@Entity
@Table(name = "stock_reservations")
@Getter
@Setter
public class StockReservationEntity {

    @Id
    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false, length = 20)
    private String status; // RESERVED | RELEASED | REJECTED

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "stock_reservation_lines",
            joinColumns = @JoinColumn(name = "reservation_id"))
    private List<ReservedLine> lines = new ArrayList<>();

    protected StockReservationEntity() {
    }

    public StockReservationEntity(UUID reservationId, UUID orderId, String status) {
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.status = status;
    }

    @Embeddable
    @Getter
    @Setter
    public static class ReservedLine {
        @Column(name = "product_id")
        private UUID productId;

        @Column(name = "quantity")
        private int quantity;

        protected ReservedLine() {
        }

        public ReservedLine(UUID productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
