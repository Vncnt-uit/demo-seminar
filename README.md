# demo-seminar — UML → Code (MVC Monolith vs Microservices)

Bộ mock code để chiếu cạnh nhau khi seminar. Code **không cần chạy**; mỗi file
được chú thích để chỉ ra nó đến từ hình UML nào.

```
docs/                 # 2 file .docx gốc (phần 2 và phần 3) + hình UML
demo-monolith/        # M1–M4 (+ M5/M6 suy ra): Package / Class / Sequence
demo-microservices/   # S1–S6: Component / Class / Sequence / Deployment
MAPPING.md            # bảng nối hình ↔ file + bảng khác biệt 2 kiến trúc
```

## Xem nhanh

| Muốn chiếu | Mở |
|---|---|
| Package Diagram | `demo-monolith/src/main/java/com/project/ecommerce/` |
| Class Diagram monolith | `demo-monolith/.../model/entity/OrderEntity.java` |
| `@Transactional` + in-process call | `demo-monolith/.../service/OrderService.java` |
| Component Diagram | `demo-microservices/` (6 thư mục service) |
| Class Diagram phân tán | `demo-microservices/order-service/.../model/OrderEntity.java` |
| Saga + compensating | `demo-microservices/*/.../event/*Listener.java` |
| Deployment Diagram | `demo-microservices/docker-compose.yml` (14 container, 9 network) |
| Deployment — đối chiếu | `demo-monolith/docker-compose.yml` (2 container, 1 network) |
| Bảng đối chiếu | `MAPPING.md` |

## Câu chốt

Cùng một nghiệp vụ "đặt hàng", monolith dùng **12 message, 1 transaction**;
microservices dùng **29 message, 4 local transaction + 2 nhánh bù trừ**.

- Monolith: đơn giản, ACID, debug dễ, nhưng coupling và scale toàn hệ thống.
- Microservices: scale độc lập, ranh giới dữ liệu rõ, nhưng phải trả giá bằng
  network latency, idempotency, Saga và hạ tầng vận hành.

Xem `MAPPING.md` mục 5 để có bảng trade-off đầy đủ.
