-- ============================================================================
-- [UML: Component Diagram] Notification Service -> "Notification logs"
-- [ARCH: DATABASE-PER-SERVICE] order_id is a reference value, not a FK.
-- ============================================================================

CREATE TABLE notification_logs (
    id       UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,   -- idempotency guard for at-least-once delivery
    channel  VARCHAR(20) NOT NULL,
    status   VARCHAR(20) NOT NULL,
    sent_at  TIMESTAMPTZ NOT NULL
);
