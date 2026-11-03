# Event contracts — Message Broker (RabbitMQ)

Exchange: `shop.events` (topic). Moi routing key duoi day tuong ung mot mui ten
tren **Sequence Diagram phan 3** va mot "Required/Provided Interface" tren
**Component Diagram**.

| Routing key | Event | Producer | Consumer(s) | Sequence msg | Queue |
|---|---|---|---|---|---|
| `order.created` | `OrderCreatedEvent` | Order Service | Inventory Service | 5 -> 8 | `inventory.order-created` |
| `stock.reserved` | `StockReservedEvent` | Inventory Service | Payment Service | 11 -> 12 | `payment.stock-reserved` |
| `stock.rejected` | `StockRejectedEvent` | Inventory Service | Order Service | 27 -> 28 | `order.stock-rejected` |
| `payment.succeeded` | `PaymentSucceededEvent` | Payment Service | Order Service, Notification Service | 15 -> 16, 18 | `order.payment-succeeded`, `notification.payment-succeeded` |
| `payment.failed` | `PaymentFailedEvent` | Payment Service | Inventory Service, Order Service | 21 -> 22, 24 | `inventory.payment-failed`, `order.payment-failed` |

## Payloads

```json
// order.created
{
  "orderId": "4f1c...",
  "customerId": "9ab2...",
  "totalAmount": 249.90,
  "items": [
    { "productId": "77de...", "quantity": 2, "unitPrice": 99.95 }
  ]
}
```

```json
// stock.reserved  (amount is carried so Payment never reads Order DB)
{ "orderId": "4f1c...", "reservationId": "b3a1...", "amount": 249.90 }
```

```json
// stock.rejected
{ "orderId": "4f1c...", "reason": "Insufficient stock for 77de..." }
```

```json
// payment.succeeded
{ "orderId": "4f1c...", "paymentId": "e901..." }
```

```json
// payment.failed
{ "orderId": "4f1c...", "reason": "gateway-unavailable" }
```

## Rules that the diagrams do not show (but code must handle)

- **At-least-once**: RabbitMQ can redeliver. Every consumer in this repo is
  idempotent (unique `order_id` in reservations/payments, status guard in Order,
  `existsByOrderId` in Notification).
- **No shared entity classes**: each service has its own copy of the event
  record. Sharing a JPA entity would recreate the monolith coupling.
- **Publish after commit**: the mock publishes right after the local transaction;
  production should use a Transactional Outbox so a committed row and its event
  cannot diverge (doc 4.2).
- **Ordering**: the saga is a chain of independent local transactions; there is
  no global order guarantee, only the causal chain order.created -> stock.reserved
  -> payment.succeeded/failed.
