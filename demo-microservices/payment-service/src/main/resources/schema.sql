-- ============================================================================
-- [UML: Class Diagram - MICROSERVICES]  ->  Payments DB ONLY
--   Payment (Id, OrderId, Amount, Status)
--
-- [ARCH: DATABASE-PER-SERVICE] order_id is UNIQUE (idempotency) but NOT a FK:
-- the orders table belongs to Order Service and this database cannot see it.
-- ============================================================================

CREATE TABLE payments (
    id         UUID PRIMARY KEY,
    order_id   UUID NOT NULL UNIQUE,
    amount     NUMERIC(12,2) NOT NULL,
    status     VARCHAR(20) NOT NULL,   -- SUCCEEDED | FAILED | REFUNDED
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
