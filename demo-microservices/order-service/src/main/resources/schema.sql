-- ============================================================================
-- [UML: Class Diagram - MICROSERVICES]  ->  Order DB ONLY
--   Order (1) <#>-- OrderItem (1..*)  : composition kept, FK is LOCAL
--   Order.CustomerId                  : plain UUID, NO foreign key
--   OrderItem.ProductId / UnitPrice   : plain values, NO foreign key
--
-- [ARCH: DATABASE-PER-SERVICE]
--   There is deliberately no `customers` / `products` / `payments` table here.
--   Compare with demo-monolith/src/main/resources/schema.sql.
-- ============================================================================

CREATE TABLE orders (
    id           UUID PRIMARY KEY,
    customer_id  UUID NOT NULL,          -- reference to Users DB, no FK
    total_amount NUMERIC(12,2) NOT NULL,
    status       VARCHAR(20) NOT NULL,   -- PENDING | CONFIRMED | FAILED | REJECTED
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
    id         UUID PRIMARY KEY,
    order_id   UUID NOT NULL,
    product_id UUID NOT NULL,            -- reference to Inventory DB, no FK
    quantity   INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL,   -- price snapshot at order time
    -- [UML: Composition] local FK, allowed because OrderItem is in the same service
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
