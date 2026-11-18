# 🏛️ Architecture Governance & AI Bounded Context Report

> **CI/CD Quality Gate**: 🟢 **PASSED** | **Architecture Health Score**: `100/100` | **AI Mode**: `OFFLINE_EXPERT_AI`

## 1. Executive Summary
Kiến trúc hệ thống tuân thủ 100% ranh giới Bounded Context theo chuẩn Domain-Driven Design (DDD).

### Key Architectural Insights:
- Các service hoàn toàn tách biệt dữ liệu theo mẫu Database-per-Service.
- Giao tiếp đồng bộ chỉ giới hạn qua HTTP REST Client với Data Transfer Objects (DTO) tối thiểu.
- Các thay đổi trạng thái liên service được điều phối thông qua sự kiện bất đồng bộ (Saga Choreography) trên Message Broker.
- Không phát hiện tình trạng rò rỉ Aggregate Root hay thực thể nội bộ (Entity Leakage).

## 2. Reverse-Engineered Dependency Graph (Actual Architecture)
```mermaid
flowchart TD
    %% Architectural Dependency Graph (Reverse-Engineered from Source Code)
    subgraph Ingress [Client & Gateway]
        Client["📱 Web / Mobile Client"]
        Gateway["🚪 API Gateway (Spring Cloud)"]
        Client --> Gateway
    end
    Broker[["🐰 RabbitMQ Message Broker (Async Events)"]]
    subgraph BoundedContexts [DDD Bounded Contexts]
        user_service["<b>User Context</b><br/><small>Entities: CustomerEntity</small>"]
        payment_service["<b>Payment Context</b><br/><small>Entities: Payment</small>"]
        order_service["<b>Order Context</b><br/><small>Entities: Order, OrderItem</small>"]
        notification_service["<b>Notification Context</b><br/><small>Entities: NotificationLogEntity</small>"]
        inventory_service["<b>Inventory Context</b><br/><small>Entities: InventoryItem, StockReservation</small>"]
    end
    Broker -->|Consume Event| payment_service
    order_service -. "HTTP REST" .-> inventory_service
    order_service -. "HTTP REST" .-> user_service
    order_service -->|Publish Event| Broker
    Broker -->|Consume Event| order_service
    Broker -->|Consume Event| notification_service
    Broker -->|Consume Event| inventory_service
    Gateway -. "HTTP REST" .-> user_service
    Gateway -. "HTTP REST" .-> payment_service
    Gateway -. "HTTP REST" .-> order_service
    Gateway -. "HTTP REST" .-> notification_service
    Gateway -. "HTTP REST" .-> inventory_service

    %% Styling
    classDef clean fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    classDef broker fill:#fff3e0,stroke:#f57c00,stroke-width:2px,stroke-dasharray: 3 3;
    classDef violation fill:#ffebee,stroke:#d32f2f,stroke-width:3px;
    class Broker broker;
```

## 3. ✅ Ranh giới Bounded Context hoàn toàn sạch sẽ!
Mọi dịch vụ tuân thủ nguyên tắc **Database-per-Service** và không có hiện tượng rò rỉ mã nguồn giữa các ngữ cảnh.