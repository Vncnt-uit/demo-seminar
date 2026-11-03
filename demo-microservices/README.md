# demo-microservices — Component / Deployment (UML phần 3)

Mỗi thư mục con là **một component = một artifact = một chủ sở hữu dữ liệu**.
Bên trong mỗi service vẫn là **Controller → Service → Repository** (MVC trong
một service) — đúng như ý "Microservices không loại trừ MVC" trong tài liệu.

| Thư mục | UML (Component Diagram) | DB sở hữu | Port |
|---|---|---|---|
| `api-gateway/` | API Gateway, không dữ liệu nghiệp vụ | — | 8080 |
| `order-service/` | Order Service | `order-db` | 8081 |
| `inventory-service/` | Inventory Service | `inventory-db` | 8082 |
| `payment-service/` | Payment Service | `payment-db` | 8083 |
| `notification-service/` | Notification Service (chỉ consume event) | `notification-db` | 8084 |
| `user-service/` | User Service (có trên hình, thiếu trong bảng 2.1) | `users-db` | 8085 |
| `rabbitmq` (compose) | Message Queue | — | 5672 / 15672 |
| `payment-gateway-mock` | External System: Payment Gateway | — | 9090 |
| `mailhog` | External System: SMTP | — | 1025 / 8025 |

## Cây thư mục

```
demo-microservices/
├── docker-compose.yml          # Deployment Diagram + database-per-service
├── pom.xml                     # parent: 6 module, không share domain class
├── contracts/
│   ├── events.md               # routing key, payload, idempotency
│   └── api.md                  # provided interfaces
├── api-gateway/                # chỉ route /api/orders, /api/users
├── order-service/
│   └── src/main/java/com/shop/order/
│       ├── controller/         # Provided Interface (POST /api/orders)
│       ├── service/            # @Transactional LOCAL + publish OrderCreated
│       ├── repository/         # chỉ order_db
│       ├── model/              # Order, OrderItem (CustomerId là UUID)
│       ├── client/             # InventoryClient, UserClient  (network call)
│       └── event/              # publisher + 3 listener kết quả Saga
├── inventory-service/          # reserve / release (compensation)
├── payment-service/            # charge idempotent + refund + gateway client
├── notification-service/       # chỉ có listener, không có controller
└── user-service/               # Customers DB
```

## Đọc mapping ở đâu

Mỗi file có header:

```
// [UML: Component Diagram] ...
// [UML: Sequence Diagram] msg 8 ...
// [ARCH: MICROSERVICES] ...
```

Bảng đối chiếu đầy đủ với monolith: `../MAPPING.md`.

## Ghi chú chạy thử

Các image `seminar/*:mock` là **minh hoạ**. `docker-compose.yml` dùng để chiếu
topology (5 DB, 9 network, gateway chỉ expose 8080), không phải để `up` ngay.
Muốn chạy thật: build từng module bằng Maven rồi tag image tương ứng.
Kiểm tra cú pháp file compose:

```bash
docker compose config
```

## Luồng đặt hàng (Saga choreography)

```
Client -> Gateway -> Order Service
                         |  (local tx: order PENDING + outbox)
                         v
                    order.created
                         v
                  Inventory Service  -- stock.reserved --> Payment Service
                         |                                   |
                    stock.rejected                    payment.succeeded
                         |                                   |
                         +----------> Order Service <--------+
                                          |
                                          +--> Notification Service
```

Nhánh lỗi:

- `payment.failed` → Inventory **release** hàng đã giữ + Order mark `FAILED`.
- `stock.rejected` → Order mark `REJECTED` (không có gì để hoàn tác).

Đây là lý do không có `@Transactional` nào bao trùm 5 service: mỗi mũi tên là
một transaction cục bộ, cộng với compensating action.
