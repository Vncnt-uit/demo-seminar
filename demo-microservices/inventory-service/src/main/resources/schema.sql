-- ============================================================================
-- [UML: Class Diagram - MICROSERVICES]  ->  Inventory DB ONLY
--   InventoryItem (ProductId, AvailableQuantity)
--   StockReservation (ReservationId, OrderId, Status)
--
-- [ARCH: DATABASE-PER-SERVICE]
--   stock_reservations.order_id is a PLAIN UUID. There is no FK to orders,
--   because the orders table lives in another database / another service.
-- ============================================================================

CREATE TABLE inventory_items (
    product_id         UUID PRIMARY KEY,
    available_quantity INTEGER NOT NULL CHECK (available_quantity >= 0),
    unit_price         NUMERIC(12,2) NOT NULL
);

CREATE TABLE stock_reservations (
    reservation_id UUID PRIMARY KEY,
    order_id       UUID NOT NULL UNIQUE,   -- reference only, NO cross-DB FK
    status         VARCHAR(20) NOT NULL,   -- RESERVED | RELEASED | REJECTED
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE stock_reservation_lines (
    reservation_id UUID NOT NULL,
    product_id     UUID NOT NULL,
    quantity       INTEGER NOT NULL,
    CONSTRAINT fk_lines_reservation FOREIGN KEY (reservation_id)
        REFERENCES stock_reservations(reservation_id) ON DELETE CASCADE
);
