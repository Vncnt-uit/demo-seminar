# API contracts — provided interfaces

| HTTP | Service | UML element | Auth / exposure |
|---|---|---|---|
| `POST /api/orders` | Order Service (via Gateway) | Component «Order Service» Provided Interface; Sequence msg 1-2 | public, returns **202 Accepted** |
| `GET /api/orders/{id}` | Order Service (via Gateway) | read model of the saga status | public |
| `GET /api/users/{id}` | User Service (via Gateway) | Component «User Service» Provided Interface; replaces the monolith's `OrderEntity.customer` association | public-ish (internal in production) |
| `GET /api/inventory/{productId}` | Inventory Service (internal) | Class Diagram dependency **"ProductId via API"** | internal only |
| `GET /api/payments/{orderId}` | Payment Service (internal) | Component «Payment Service» Provided Interface | internal only |
| `POST /api/payments/{orderId}/refund` | Payment Service (internal) | compensating transaction (doc 4.3) | internal only |

## Request / response — `POST /api/orders`

```json
// request
{
  "customerId": "9ab2...",
  "items": [
    { "productId": "77de...", "quantity": 2 }
  ]
}
```

```json
// 202 Accepted
{
  "orderId": "4f1c...",
  "status": "PENDING",
  "totalAmount": 249.90,
  "customerName": "Nguyen Van A"
}
```

Differences vs the monolith API (`demo-monolith`):

| | Monolith | Microservices |
|---|---|---|
| Status code | `200 OK` | `202 Accepted` |
| Order status in response | `CONFIRMED` | `PENDING` |
| When the flow finishes | before the response | after the Saga events |
| Who owns the price | central `products` table | Inventory Service, snapshotted into `OrderItem.UnitPrice` |
| Who owns the customer name | FK join in the same DB | HTTP call to User Service |
