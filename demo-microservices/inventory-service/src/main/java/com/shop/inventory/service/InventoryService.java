package com.shop.inventory.service;

import com.shop.inventory.event.OrderCreatedEvent;
import com.shop.inventory.model.InventoryItemEntity;
import com.shop.inventory.model.StockReservationEntity;
import com.shop.inventory.repository.InventoryItemRepository;
import com.shop.inventory.repository.StockReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// =============================================================================
// [UML: Sequence Diagram] lifeline "Inventory Service"
//   msg 8  receive OrderCreated
//   msg 9  self-call "Check and reserve stock"
//   msg 11 publish StockReserved      (alt [Stock available])
//   msg 27 publish StockRejected      (alt [Out of stock])
//   msg 22 receive PaymentFailed
//   msg 23 self-call "Release reserved stock"   <-- COMPENSATING ACTION
//
// [ARCH: MICROSERVICES] reserve() commits its own local transaction. If payment
// later fails, release() is a NEW transaction that adds the quantity back.
// There is no database rollback across services -- this is the Saga pattern.
// =============================================================================
@Service
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final StockReservationRepository reservationRepository;

    public InventoryService(InventoryItemRepository itemRepository,
                            StockReservationRepository reservationRepository) {
        this.itemRepository = itemRepository;
        this.reservationRepository = reservationRepository;
    }

    // [UML: Sequence msg 9] local transaction on Inventory DB.
    @Transactional
    public ReservationResult reserve(OrderCreatedEvent event) {

        // Idempotency (doc 4.2): the same OrderCreated may be delivered twice.
        var existing = reservationRepository.findByOrderId(event.orderId());
        if (existing.isPresent()) {
            StockReservationEntity reservation = existing.get();
            return new ReservationResult(
                    "RESERVED".equals(reservation.getStatus()),
                    reservation.getReservationId(),
                    reservation.getStatus());
        }

        // 1) Validate ALL lines first, so a partial reservation is never committed.
        List<InventoryItemEntity> items = event.items().stream()
                .map(line -> itemRepository.findById(line.productId()).orElse(null))
                .toList();

        for (int i = 0; i < event.items().size(); i++) {
            OrderCreatedEvent.Line line = event.items().get(i);
            InventoryItemEntity item = items.get(i);
            if (item == null || item.getAvailableQuantity() < line.quantity()) {
                // No stock change. Persist a REJECTED reservation for audit + idempotency.
                StockReservationEntity rejected = new StockReservationEntity(
                        UUID.randomUUID(), event.orderId(), "REJECTED");
                reservationRepository.save(rejected);
                return new ReservationResult(false, rejected.getReservationId(),
                        "Insufficient stock for " + line.productId());
            }
        }

        // 2) Decrement the stock and record what was reserved (needed to compensate).
        StockReservationEntity reservation = new StockReservationEntity(
                UUID.randomUUID(), event.orderId(), "RESERVED");
        for (int i = 0; i < event.items().size(); i++) {
            OrderCreatedEvent.Line line = event.items().get(i);
            InventoryItemEntity item = items.get(i);
            item.setAvailableQuantity(item.getAvailableQuantity() - line.quantity());
            reservation.getLines().add(new StockReservationEntity.ReservedLine(
                    line.productId(), line.quantity()));
        }
        reservationRepository.save(reservation);
        return new ReservationResult(true, reservation.getReservationId(), "RESERVED");
    }

    // [UML: Sequence msg 23] compensating action, NOT a rollback.
    @Transactional
    public void release(UUID orderId) {
        reservationRepository.findByOrderId(orderId).ifPresent(reservation -> {
            if (!"RESERVED".equals(reservation.getStatus())) {
                return; // already released / rejected -> idempotent
            }
            for (StockReservationEntity.ReservedLine line : reservation.getLines()) {
                itemRepository.findById(line.getProductId()).ifPresent(item ->
                        item.setAvailableQuantity(item.getAvailableQuantity() + line.getQuantity()));
            }
            reservation.setStatus("RELEASED");
        });
    }

    public record ReservationResult(boolean reserved, UUID reservationId, String reason) {
    }
}
