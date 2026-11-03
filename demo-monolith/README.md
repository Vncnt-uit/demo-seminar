# demo-monolith — MVC API-First / Layered (UML part 2)

Mo file nay trong IDE: cay thu muc duoi day chinh la **Package Diagram** cua
phan 2 (controller / service / repository / model / config).

```
com.project.ecommerce/
├── controller/        # OrderController, PaymentController
├── service/           # OrderService, InventoryService, PaymentService,
│                      # NotificationService, PaymentGatewayClient
├── repository/        # OrderRepository, ProductRepository, PaymentRepository,
│                      # CustomerRepository (suy ra tu Association)
├── model/
│   ├── entity/        # OrderEntity, OrderItemEntity, CustomerEntity,
│   │                  # ProductEntity, PaymentEntity
│   └── dto/           # OrderRequestDTO, OrderItemRequest, PaymentInfo,
│                      # OrderResponseDTO
└── config/            # GlobalExceptionHandler (Infrastructure Layer)
```

## Cach doc mapping

Moi file co header dang:

```
// [UML: Package Diagram] ...
// [UML: Class Diagram] ...
// [UML: Sequence Diagram] msg N ...
// [ARCH: MONOLITH] ...
```

- `[UML: ...]` = thanh phan nao cua hinh ve ma file nay hien thuc.
- `msg N` = so thu tu tren **Sequence Diagram phan 2** (12 message).
- `[ARCH: MONOLITH]` = hanh vi chi dung duoc vi tat ca nam trong 1 tien trinh
  + 1 database.

## Diem khac biet cot loi

`OrderService.processOrder()` duoc bao boi **mot** `@Transactional`. Ben trong no
goi `InventoryService`, `PaymentService`, `OrderRepository` bang loi goi ham
Java. Neu bat ky buoc nao nem exception, ca 4 bang (orders, order_items,
products, payments) rollback cung luc.

Ban microservices tuong ung: `../demo-microservices/order-service/.../OrderService.java`
(khong co transaction xuyen service, chi co Saga + compensating action).

Xem bang doi chieu day du tai `../MAPPING.md`.

## Deployment

`docker-compose.yml` trong thu muc nay chi co **2 container** (app + 1 database),
doi chieu voi `../demo-microservices/docker-compose.yml` (**14 container**,
9 network). Mo ca hai file canh nhau de thay ro su khac biet ve ha tang.

