# UML → Code: Monolith (MVC API-first) vs Microservices

Tài liệu này là bản đồ nối **từng hình UML** trong `docs/` với **từng file mock**
trong `demo-monolith/` và `demo-microservices/`, và chỉ ra chỗ hai kiến trúc
khác nhau *trên bản vẽ* lẫn *trong code*.

Code không cần chạy được. Mục tiêu là: mở file nào cũng thấy nó đến từ hình nào.

> Phần 1 (Use Case + Activity Diagram) không sinh code: nó xác định actor
> (Customer, Payment Gateway, Email/SMS provider) và 4 vùng nghiệp vụ
> (Order / Inventory / Payment / Notification). Đó là **input** để tách service;
> kết quả nằm ở `demo-microservices/` (mục 3 bên dưới).

---

## 0. Cách đọc comment trong code

Mỗi file bắt đầu bằng một header. Các nhãn dùng xuyên suốt:

| Nhãn | Nghĩa |
|---|---|
| `[UML: Package Diagram]` | Gói / lớp / mũi tên "uses" trên Package Diagram (phần 2) |
| `[UML: Class Diagram]` | Lớp, thuộc tính, association / composition / dependency |
| `[UML: Component Diagram]` | Component, Provided/Required Interface, boundary (phần 3) |
| `[UML: Sequence Diagram] msg N` | Đúng số thứ tự message trên Sequence Diagram |
| `[UML: Deployment Diagram]` | Node, Execution Environment, Artifact, Database Node, External System |
| `[ARCH: MONOLITH]` | Hành vi chỉ đúng khi mọi thứ nằm trong 1 tiến trình + 1 DB |
| `[ARCH: MICROSERVICES]` | Hành vi chỉ đúng khi service tách process + DB riêng |
| `[ARCH: DIFFERENCE]` | Chỗ hai bên cố tình làm khác nhau — dùng để chiếu slide |

Ví dụ trong `OrderService` của monolith:

```java
// [UML: Sequence msg 3] in-process call: OrderService -> InventoryService
inventoryService.checkAndReserveStock(request.getItems());
```

và bản microservices:

```java
// [UML: Sequence msg 5] Publish OrderCreated to the Message Broker.
eventPublisher.publishOrderCreated(OrderCreatedEvent.from(order));
```

---

## 1. Kho hình UML và nơi nó được hiện thực

### 1.1 Phần 2 — Monolith (4 hình + 2 mục suy ra từ tài liệu)

| # | Hình | Nội dung | Code tương ứng |
|---|---|---|---|
| M1 | Package Diagram | `controller`, `service`, `repository`, `model` (+ 3 mũi tên "uses") | toàn bộ `demo-monolith/src/main/java/com/project/ecommerce/` |
| M2 | Luồng API-first | SPA → REST Controller → Service → Repository → Central DB | `controller/` → `service/` → `repository/` → `application.yml` |
| M3 | Class Diagram | `OrderController`, `OrderService`, `OrderEntity`, `CustomerEntity`, `OrderItemEntity` | `controller/OrderController.java`, `service/OrderService.java`, `model/entity/*` |
| M4 | Sequence Diagram | 7 lifeline, 12 message, khung `@Transactional` | `service/OrderService.java` + `service/InventoryService.java` + `service/PaymentService.java` |
| M5 | Deployment (suy ra) | 1 JVM + 1 Central Database | `EcommerceMonolithApplication.java` + `application.yml` + `docker-compose.yml` |
| M6 | Exception path | Exception → rollback → 400 | `config/GlobalExceptionHandler.java` |

### 1.2 Phần 3 — Microservices (6 hình)

| # | Hình | Nội dung | Code tương ứng |
|---|---|---|---|
| S1 | Component Diagram | Client, API Gateway, Order/User/Payment/Inventory Service, Message Queue, Notification, 5 DB | `docker-compose.yml` + 6 thư mục service |
| S2 | Class Diagram phân tán | 3 boundary: OrderService, InventoryService, PaymentService | `order-service/model/*`, `inventory-service/model/*`, `payment-service/model/*` |
| S3 | Sequence Diagram (Saga) | 8 lifeline, 29 message, 2 khối `alt` lồng nhau | các `*Listener.java` + `*Service.java` |
| S4 | Deployment Diagram | Client → Gateway → Services, broker, DB riêng, `REST / gRPC` | `docker-compose.yml` (networks + services) |
| S5 | Layered trong 1 service | Controller → Service → Repository → Data Store | cấu trúc bên trong **mỗi** service |
| S6 | Deployment Kubernetes | Load Balancer → API Gateway → Pods → Infrastructure | ghi chú đối chiếu trong `docker-compose.yml` (K8s là tuỳ chọn) |

---

## 2. Monolith: hình → file

### 2.1 Package Diagram → cây thư mục

| Gói UML | Thư mục | File |
|---|---|---|
| `controller` | `controller/` | `OrderController.java`, `PaymentController.java` |
| `service` | `service/` | `OrderService.java`, `InventoryService.java`, `PaymentService.java`, `NotificationService.java`, `PaymentGatewayClient.java` (helper hạ tầng, không vẽ trên hình) |
| `repository` | `repository/` | `OrderRepository.java`, `ProductRepository.java`, `PaymentRepository.java`, `CustomerRepository.java` (suy ra từ Association) |
| `model.entity` | `model/entity/` | `OrderEntity`, `OrderItemEntity`, `CustomerEntity` + `ProductEntity`, `PaymentEntity` (chỉ có trong cây thư mục) |
| `model.dto` | `model/dto/` | `OrderRequestDTO`, `OrderResponseDTO` + `OrderItemRequest`, `PaymentInfo` (helper type) |
| `config` | `config/` | `GlobalExceptionHandler.java` |

> Package Diagram chỉ vẽ 2 lớp trong `model` (`OrderRequestDTO`, `OrderEntity`)
> để đơn giản hoá. Cây thư mục ở "Bước 3" của tài liệu liệt kê đủ 5 entity + 2
> DTO, còn Class Diagram bổ sung `CustomerEntity` và `OrderItemEntity` — code
> theo cây thư mục + Class Diagram.

Ba mũi tên nét đứt **"uses"** trên Package Diagram:

| Mũi tên UML | Dòng code |
|---|---|
| `OrderController ---> OrderService` | `OrderController` giữ `private final OrderService orderService` + constructor injection |
| `OrderService ---> OrderRepository` | `OrderService` giữ `private final OrderRepository orderRepository` |
| `OrderService ---> OrderEntity` | `processOrder(...)` tạo và trả về `OrderEntity` / `OrderResponseDTO` |

### 2.2 Class Diagram → Entity & DTO

Hình M3 có 5 lớp. Code tương ứng:

| Lớp UML | File | Ghi chú |
|---|---|---|
| `OrderController` | `controller/OrderController.java` | operation `+createOrder(request: OrderRequestDTO): OrderResponseDTO` |
| `OrderService` | `service/OrderService.java` | operation `+processOrder(...)` |
| `OrderEntity` | `model/entity/OrderEntity.java` | `id`, `totalPrice`, `status`, `customer`, `items` |
| `CustomerEntity` | `model/entity/CustomerEntity.java` | `id`, `name`, `email` |
| `OrderItemEntity` | `model/entity/OrderItemEntity.java` | `id`, `productId`, `quantity` |

Hai quan hệ quan trọng nhất của hình M3:

| Quan hệ UML | Multiplicity | Code | DB |
|---|---|---|---|
| **Association** `OrderEntity` → `CustomerEntity` | many → 1 | `@ManyToOne` + `@JoinColumn(name = "customer_id")` | FK `orders.customer_id` |
| **Composition** `OrderEntity` ◆→ `OrderItemEntity` | 1 → many | `@OneToMany(cascade = ALL, orphanRemoval = true)` + `@JoinColumn(name = "order_id")` | FK `order_items.order_id` + `ON DELETE CASCADE` |
| **Dependency** `OrderController` ⇢ `OrderService` | — | constructor injection (`private final`) | — |

`OrderService ---> OrderEntity` trên hình ghi **"Manages"** — không phải ký hiệu
chuẩn của UML; trong code nó là kiểu tham số/kiểu trả về của `processOrder`.

### 2.3 Sequence Diagram → in-process calls

7 lifeline của hình M4 ↔ 7 thành phần chạy trong **cùng một JVM**:

| Lifeline | Code |
|---|---|
| `Client (SPA/Mobile)` | bên ngoài, không có class |
| `OrderController` | `@RestController` |
| `OrderService (@Transactional)` | `@Service` + `@Transactional` |
| `InventoryService` | `@Service` |
| `PaymentService` | `@Service` |
| `OrderRepository` | Spring Data interface |
| `Database` | `spring.datasource` (một URL) |

12 message ↔ code:

| Msg | Trên hình | Trong code |
|---:|---|---|
| 1 | `POST /api/orders (OrderRequestDTO)` | `OrderController.createOrder(...)` |
| 2 | `processOrder(requestDTO)` | `orderService.processOrder(request)` |
| 3 | `checkAndReserveStock(items)` | gọi method trực tiếp |
| 4 | `Stock Reserved` | method return bình thường |
| 5 | `processPayment(paymentInfo)` | gọi method trực tiếp |
| 6 | `Payment Success` | method return bình thường |
| 7 | `save(orderEntity)` | `orderRepository.save(order)` |
| 8 | `INSERT INTO orders ...` | Hibernate sinh SQL |
| 9 | `Order Saved` | JDBC return |
| 10 | `OrderEntity` | `saved` |
| 11 | `OrderResponseDTO` | `return new OrderResponseDTO(...)` |
| 12 | `200 OK (JSON)` | `ResponseEntity.ok(...)` |

Khung transaction lớn trên hình **chính là** `@Transactional` trên
`OrderService.processOrder`. Không có transaction nào khác.

### 2.4 Exception path

Hình M4 không vẽ nhánh lỗi, nhưng tài liệu phần 2 mô tả: exception → rollback →
`@ControllerAdvice`. Code: `config/GlobalExceptionHandler.java`
(`@ExceptionHandler(RuntimeException.class)` → 400).

---

## 3. Microservices: hình → file

### 3.1 Component Diagram → service boundary

| Thành phần UML | Code | Dữ liệu sở hữu |
|---|---|---|
| `Client` | ngoài hệ thống | — |
| `API Gateway` | `api-gateway/` | không có datasource |
| `Order Service` | `order-service/` | Orders DB |
| `Inventory Service` | `inventory-service/` | Inventory DB |
| `Payment Service` | `payment-service/` | Payments DB |
| `Notification Service` | `notification-service/` | Notification DB |
| `User Service` | `user-service/` | Users DB |
| `Message Queue` | `rabbitmq` trong compose | — |
| `Per-Service Databases` | 5 container `*-db` | — |

Provided / Required Interface → code:

| Khái niệm UML | Order | Inventory | Payment | Notification | User |
|---|---|---|---|---|---|
| **Provided** | `POST /api/orders`, `GET /api/orders/{id}` | `GET /api/inventory/{productId}` | `GET /api/payments/{orderId}`, `POST .../refund` | không có HTTP | `GET /api/users/{id}` |
| **Required** | Inventory API + User API + broker | broker | broker + Payment Gateway | broker + SMTP | — |
| Code | `controller/OrderController`, `client/*` | `InventoryController` | `PaymentController` | `*Listener` | `UserController` |

> Điểm quan trọng: Component Diagram có **User Service** nhưng bảng 2.1 trong
> tài liệu không liệt kê. Code vẫn tạo `user-service` để khớp **hình**, và ghi
> rõ chỗ thiếu trong mục 6.

### 3.2 Class Diagram phân tán → Database-per-Service

| Boundary | Lớp | File | Quan hệ |
|---|---|---|---|
| `OrderService` | `Order` | `order-service/model/OrderEntity.java` | `Id`, `CustomerId`, `TotalAmount`, `Status` |
| `OrderService` | `OrderItem` | `order-service/model/OrderItemEntity.java` | `Id`, `OrderId`, `ProductId`, `Quantity`, `UnitPrice` |
| `OrderService` | composition `Order ◆→ OrderItem` | `@OneToMany` + `@JoinColumn` | **giữ được**, vì cùng service + cùng DB |
| `InventoryService` | `InventoryItem` | `inventory-service/model/InventoryItemEntity.java` | `ProductId`, `AvailableQuantity` |
| `InventoryService` | `StockReservation` | `inventory-service/model/StockReservationEntity.java` | `ReservationId`, `OrderId`, `Status` |
| `InventoryService` | association `InventoryItem → StockReservation` ("reserved for", 1 → 0..*) | `StockReservationEntity.ReservedLine` (`productId` + `quantity`) | hình vẽ 1 reservation gắn 1 item; code mở rộng thành nhiều dòng cho đơn nhiều sản phẩm |
| `PaymentService` | `Payment` | `payment-service/model/PaymentEntity.java` | `Id`, `OrderId`, `Amount`, `Status` |

Hai mũi tên nét đứt giữa các boundary:

| Mũi tên UML | Nhãn | Code |
|---|---|---|
| `OrderService ⇢ InventoryService` | `ProductId via API` | `client/InventoryClient.java` (REST) + `InventoryController` |
| `OrderService ⇢ PaymentService` | `OrderId via API or event` | trong luồng này chọn **event**: `OrderEventPublisher` → `StockReservedListener` |

Sự khác biệt **cốt lõi** so với hình M3:

```java
// MONOLITH — association thật, FK thật
@ManyToOne @JoinColumn(name = "customer_id")
private CustomerEntity customer;

// MICROSERVICES — chỉ còn ID, không FK, không navigation
@Column(name = "customer_id")
private UUID customerId;
```

### 3.3 Sequence Diagram (Saga choreography) → listener

8 lifeline ↔ code:

| Lifeline | Code |
|---|---|
| `Customer` | ngoài hệ thống |
| `API Gateway` | `api-gateway/` |
| `Order Service` | `order-service/` |
| `Order DB` | `order-db` |
| `Message Broker` | `rabbitmq` |
| `Inventory Service` | `inventory-service/` |
| `Payment Service` | `payment-service/` |
| `Notification Service` | `notification-service/` |

Các mốc chính:

| Msg | Trên hình | Code |
|---:|---|---|
| 1–2 | `POST /orders`, `CreateOrder` | `OrderController.create` |
| 3–4 | `Save Pending Order` / `Order saved` | `OrderService.createOrder` + `@Transactional` (chỉ order_db) |
| 5 | `Publish OrderCreated` | `OrderEventPublisher.publishOrderCreated` |
| 6 | `202 Accepted` + `Order processing` | `ResponseEntity.accepted()` |
| 7–11 | broker → Inventory → reserve → `StockReserved` | `OrderCreatedListener` + `InventoryService.reserve` |
| 12–15 | broker → Payment → `Process payment` → `PaymentSucceeded` | `StockReservedListener` + `PaymentService.charge` |
| 16–17 | `PaymentSucceeded` → update order | `PaymentResultListener.onPaymentSucceeded` |
| 18–19 | `PaymentSucceeded` → `Send confirmation` | `PaymentSucceededListener` + `NotificationService` |
| 20–25 | nhánh `[Payment failed]` → release stock + mark failed | `PaymentFailedListener` (Inventory) + `PaymentResultListener.onPaymentFailed` |
| 26–29 | nhánh `[Out of stock]` → `StockRejected` → mark rejected | `OrderCreatedListener` (else) + `PaymentResultListener.onStockRejected` |

Hai khối `alt` trên hình trở thành `if/else` trong code:

```
alt [Stock available]        -> InventoryService.reserve() trả về reserved = true
  alt [Payment successful]   -> PaymentService.charge() trả về approved = true
  else [Payment failed]      -> publish payment.failed
else [Out of stock]          -> publish stock.rejected
```

Bảng 4 bước Saga (đối chiếu bảng 4.3 trong tài liệu) và hành động bù trừ:

| Bước | Hành động | Code | Nếu bước sau thất bại |
|---:|---|---|---|
| 1 | Tạo đơn `PENDING` | `OrderService.createOrder` | `PaymentResultListener` set `FAILED` |
| 2 | Giữ tồn kho | `InventoryService.reserve` | `InventoryService.release` (cộng lại số lượng) |
| 3 | Thanh toán | `PaymentService.charge` | `PaymentService.refund` (giao dịch mới) |
| 4 | Xác nhận đơn | `PaymentResultListener.onPaymentSucceeded` | trạng thái cuối: `CONFIRMED` / `FAILED` / `REJECTED` |

### 3.4 Deployment Diagram → docker-compose

| UML element | Trong `docker-compose.yml` |
|---|---|
| Node | Docker host |
| Execution Environment | Docker Engine, mỗi service 1 container |
| Artifact | image `seminar/<service>:mock` (jar từ `pom.xml` của service đó) |
| Deployment | mỗi entry trong `services:` |
| Communication Path | `networks:` (`edge-net`, `broker-net`, `order-inventory-net`, …) |
| Database Node | `order-db`, `inventory-db`, `payment-db`, `notification-db`, `users-db` |
| External System | `payment-gateway-mock` (WireMock), `mailhog` (SMTP) |

Network là cách **thực thi** ranh giới dữ liệu: `order-db` chỉ nằm trên
`order-db-net` cùng `order-service`. Service khác không có route tới DB đó →
không thể `JOIN` xuyên service, đúng như bản vẽ.

Hình S6 (Kubernetes) không được dựng manifest riêng. `docker-compose.yml` là
lựa chọn triển khai tương ứng hình S4; tài liệu phần 7 ghi rõ Kubernetes **không
bắt buộc**. Muốn chiếu S6: Load Balancer ↔ `ports: 8080`, Pod ↔ mỗi container,
`Kafka / RabbitMQ` ↔ `rabbitmq`.

---

## 4. Cùng một nghiệp vụ, hai luồng

Bảng dưới chiếu **12 message của monolith** sang **29 message của Saga**.

| Monolith (12 msg) | Microservices (29 msg) |
|---|---|
| 1. Client → Controller: `POST /api/orders` | 1. Customer → Gateway: `POST /orders` |
| 2. Controller → Service: gọi hàm | 2. Gateway → Order Service: `CreateOrder` (HTTP) |
| 3. Service → Inventory: gọi hàm | 3. Order Service → Order DB: `Save Pending Order` |
| 4. Inventory trả `Stock Reserved` | 4. Order DB trả `Order saved` |
| 5. Service → Payment: gọi hàm | 5. Order Service → Broker: `Publish OrderCreated` |
| 6. Payment trả `Payment Success` | 6. Gateway → Customer: `202 Accepted` |
| 7. Service → Repository: `save(order)` | 7. Broker → Inventory: `OrderCreated` |
| 8. Repository → DB: `INSERT` | 8–9. Inventory: check + reserve |
| 9. DB trả `Order Saved` | 10–11. Inventory → Broker: `StockReserved` |
| 10. Repository trả `OrderEntity` | 12–13. Broker → Payment: process |
| 11. Service trả `OrderResponseDTO` | 14–15. Payment → Broker: `PaymentSucceeded` |
| 12. Controller trả `200 OK` | 16–17. Broker → Order: update status |
| — | 18–19. Broker → Notification: send confirmation |
| — | 20–25. `[Payment failed]`: release stock + mark failed |
| — | 26–29. `[Out of stock]`: `StockRejected` + mark rejected |

Nhìn cột phải: **một** lời gọi hàm ở cột trái đã tách thành **nhiều** message
qua mạng, cộng thêm 2 nhánh lỗi không hề tồn tại trong code monolith.

---

## 5. Bảng khác biệt (dùng để chiếu slide)

| Tiêu chí | Monolith | Microservices |
|---|---|---|
| Đơn vị triển khai | 1 `ecommerce-monolith.jar` | 6 artifact: gateway + 5 service |
| Số process ứng dụng | 1 JVM | 6 container |
| Container (compose) | **2**: app + 1 DB | **14**: gateway + 5 service + 5 DB + broker + 2 external |
| Network (compose) | **1** (`app-net`) | **9**, mỗi DB một network riêng |
| Giao tiếp nội bộ | Java method call (ns–µs) | HTTP/REST + AMQP (ms, có timeout) |
| Database | 1 schema trung tâm | 5 DB, mỗi service sở hữu 1 |
| Khoá ngoại xuyên khái niệm | `orders.customer_id → customers.id` | chỉ `customer_id` (UUID), **không FK** |
| Transaction | 1 `@Transactional` bao trùm 4 bảng | mỗi service 1 local transaction |
| Khi bước sau fail | rollback toàn bộ | compensating action (Saga) |
| Trạng thái đơn khi trả response | `CONFIRMED` | `PENDING` |
| HTTP status | `200 OK` | `202 Accepted` |
| Lỗi mạng | không có | timeout / retry / circuit breaker / DLQ |
| Message trùng | không có | idempotency bắt buộc |
| Thêm 1 consumer mới | sửa code + deploy lại cả app | thêm service + bind queue |
| Scale | scale cả ứng dụng | scale riêng từng service |
| Debug | 1 stack trace | distributed tracing + correlation id |
| Nguồn coupling | compile-time (class reference) | runtime (contract/schema) |

Câu chốt cho phần so sánh: **monolith đổi sự đơn giản lấy coupling; microservices
đổi coupling lấy độ phức tạp vận hành.** Không có bên nào "đúng" tuyệt đối.

---

## 6. Những chỗ hình vẽ và code không khớp 100%

Ghi rõ để không bị hỏi khó khi demo:

1. **User Service** có trong Component Diagram (kèm `Users DB`) nhưng **không**
   có trong bảng "2.1 Xác định các service". Code tạo `user-service/` theo hình.
2. **Notification Service** nằm ngoài boundary `Services` trên hình và chỉ nối
   với Message Queue. Code làm đúng: **không có controller**, chỉ có listener.
   Hình cũng không vẽ Notification DB, nhưng bảng 2.1 ghi "Notification logs"
   → code có `notification-db`.
3. **Sequence Diagram phần 2 không có lifeline NotificationService**, dù Package
   Diagram có class này. `NotificationService.sendOrderConfirmation(...)` được
   giữ lại và đánh dấu `[not drawn on Sequence Diagram]`.
4. **Package Diagram không vẽ mũi tên nào từ `PaymentController`.** Code vẫn có
   class này (HTTP face của `PaymentService`), ghi chú rõ trong file.
5. **Class Diagram phần 2** không có `ProductEntity` / `PaymentEntity`, nhưng cây
   thư mục có. Hai entity này tồn tại và được đánh dấu "package tree only".
   `CustomerRepository` là repository thứ 4, suy ra từ Association.
6. **`UnitPrice` chỉ xuất hiện trong Class Diagram microservices**, không có ở
   bản monolith. Đây là price snapshot — monolith đọc giá từ `ProductEntity`
   trong cùng DB, nên không cần lưu lại.
7. **Mũi tên "Manages"** (`OrderService ⇢ OrderEntity`) không phải ký hiệu UML
   chuẩn. Code thể hiện bằng kiểu tham số/trả về, không phải một annotation.
8. **"ProductId via API" là sync**, trong khi luồng Saga là async. Cả hai cùng
   tồn tại: `InventoryClient` đọc giá/stock lúc tạo đơn; event `OrderCreated`
   mới là luồng Saga. Hình vẽ không tách hai việc này.
9. **Publish event trước khi transaction commit** trong code mock (chưa có
   Transactional Outbox). Tài liệu 4.2 đã cảnh báo; comment trong `OrderService`
   ghi rõ.
10. **`order_items.order_id` vẫn là FK** trong microservices. Điều này **đúng**:
    composition trong cùng một service được phép có FK. Cái bị xoá là FK
    **xuyên service** (`customer_id`, `product_id`, `payment.order_id`).
11. **Hình S6 có Kubernetes**, `docker-compose.yml` không dựng K8s. Tài liệu
    phần 7 nói K8s là lựa chọn, không phải điều kiện.
12. **Event payload** không có trong hình. Contract chi tiết nằm ở
    `demo-microservices/contracts/events.md`.
13. **Association "reserved for" (InventoryItem 1 → 0..* StockReservation)** được
    hiện thực bằng bảng `stock_reservation_lines` (`ReservedLine.productId`),
    không phải `@OneToMany` trực tiếp. Lý do: một reservation của một đơn phủ
    nhiều sản phẩm; hình khái niệm vẽ đơn giản 1 item ↔ 1 reservation.
14. **`InventoryItem.unitPrice`** là field thêm để trả giá cho
    `GET /api/inventory/{productId}`; hình chỉ vẽ `ProductId` +
    `AvailableQuantity`.
15. **Hình Kubernetes (S6) không có User Service / Users DB**, trong khi
    Component Diagram (S1) có. Code ưu tiên S1 + bảng dữ liệu 2.1, nên vẫn tạo
    `user-service` + `users-db`.

---

## 7. Kịch bản demo 5–7 phút

1. **Mở 2 folder cạnh nhau.** Trái: `com.project.ecommerce/{controller,service,
   repository,model,config}` = Package Diagram. Phải: 6 module = Component
   Diagram.
2. **Mở `OrderEntity` hai bên.** Chỉ vào `@ManyToOne CustomerEntity` (trái) và
   `UUID customerId` (phải). Đây là một dòng biến mất khi lên microservices.
3. **Mở `OrderService` hai bên.** Trái: một `@Transactional` + 3 lời gọi hàm.
   Phải: `@Transactional` chỉ order_db + `publishOrderCreated` + `202 Accepted`.
4. **Mở `PaymentResultListener` / `PaymentFailedListener`.** Chỉ vào chỗ thay
   cho rollback: `updateStatus("FAILED")` và `release(orderId)`.
5. **Mở `docker-compose.yml`.** Chỉ 5 DB, 9 network, gateway chỉ expose 8080.
   Đây là Deployment Diagram ở dạng topology kiểm tra được
   (`docker compose config`).
6. **Kết.** Monolith: 1 transaction, 1 DB, 1 artifact, debug dễ, coupling cao.
   Microservices: nhiều transaction cục bộ + Saga, nhiều DB, nhiều artifact,
   debug khó, scale độc lập.

---

## 8. File index nhanh

```
demo-monolith/
  src/main/java/com/project/ecommerce/
    EcommerceMonolithApplication.java   # 1 JVM (Deployment)
    controller/OrderController.java     # Package + Class + Sequence msg 1,2,12
    controller/PaymentController.java   # Package (không có mũi tên)
    service/OrderService.java           # @Transactional + msg 3,5,7,11
    service/InventoryService.java       # msg 3,4
    service/PaymentService.java         # msg 5,6 + External System
    service/NotificationService.java    # Package, ngoài Sequence
    repository/*.java                   # Data Access Layer
    model/entity/OrderEntity.java       # Association + Composition
    model/entity/OrderItemEntity.java   # "part" của composition
    model/dto/*.java                    # DTO vs Entity
    config/GlobalExceptionHandler.java  # exception -> 400
  src/main/resources/
    application.yml                     # 1 datasource = Central Database
    schema.sql                          # FK thật, 1 schema

demo-microservices/
  docker-compose.yml                    # Deployment Diagram + DB-per-Service
  api-gateway/                          # Component: Gateway, no data
  order-service/                        # Component + boundary OrderService
    .../service/OrderService.java       # PENDING, local tx, publish OrderCreated
    .../client/InventoryClient.java     # "ProductId via API"
    .../client/UserClient.java          # thay cho CustomerEntity
    .../event/PaymentResultListener.java# 3 nhánh kết quả Saga
  inventory-service/                    # boundary InventoryService
    .../service/InventoryService.java   # reserve + release (compensation)
    .../event/OrderCreatedListener.java # StockReserved / StockRejected
  payment-service/                      # boundary PaymentService
    .../service/PaymentService.java     # charge idempotent + refund
    .../gateway/PaymentGatewayClient.java # External System
  notification-service/                 # chỉ consume event, không controller
  user-service/                         # Users DB, CustomerId reference
  contracts/events.md                   # routing key + payload + idempotency
  contracts/api.md                      # provided interfaces
```
