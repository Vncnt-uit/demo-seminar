-- ============================================================================
-- [UML: Component Diagram] Users DB (owned by User Service)
-- [UML: Class Diagram - MONOLITH] the former CustomerEntity table.
-- [ARCH: DATABASE-PER-SERVICE] no FK from orders: Order DB cannot see this table.
-- ============================================================================

CREATE TABLE customers (
    id    UUID PRIMARY KEY,
    name  VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE
);
