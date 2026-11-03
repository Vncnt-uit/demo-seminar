-- ============================================================================
-- [UML: Class Diagram - MONOLITH]  ->  ONE relational schema
--
--   Association  OrderEntity ---> CustomerEntity
--       => orders.customer_id REFERENCES customers(id)
--   Composition  OrderEntity <#>-- OrderItemEntity
--       => order_items.order_id REFERENCES orders(id) ON DELETE CASCADE
--   PaymentEntity.order_id -> orders(id)
--
-- [ARCH: MONOLITH] Cross-entity foreign keys are possible because every table
-- lives in the SAME database. This is the property that microservices give up.
-- ============================================================================

CREATE TABLE customers (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE
);

CREATE TABLE products (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(160) NOT NULL,
    price          NUMERIC(12,2) NOT NULL,
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0)
);

CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    total_price NUMERIC(12,2) NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- [UML: Association -> FK] real foreign key, same database
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INTEGER NOT NULL CHECK (quantity > 0),
    -- [UML: Composition -> FK + CASCADE]
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE payments (
    id       BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    amount   NUMERIC(12,2) NOT NULL,
    status   VARCHAR(20) NOT NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
