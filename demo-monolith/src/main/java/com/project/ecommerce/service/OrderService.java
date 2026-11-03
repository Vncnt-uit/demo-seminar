package com.project.ecommerce.service;

import com.project.ecommerce.model.dto.OrderItemRequest;
import com.project.ecommerce.model.dto.OrderRequestDTO;
import com.project.ecommerce.model.dto.OrderResponseDTO;
import com.project.ecommerce.model.entity.*;
import com.project.ecommerce.repository.CustomerRepository;
import com.project.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// =============================================================================
// [UML: Package Diagram] package «service» -> class OrderService
//   Dependencies drawn (dashed "uses"):
//     OrderService ---> OrderRepository
//     OrderService ---> OrderEntity
//   (InventoryService / PaymentService / NotificationService are in the same
//    package, so the diagram does not draw every intra-package arrow.)
//
// [UML: Class Diagram]  +processOrder(OrderRequestDTO) : OrderResponseDTO
// [UML: Sequence Diagram] lifeline #3  "OrderService (@Transactional)"
//   The large transaction frame on the diagram == the @Transactional method below.
//
// [ARCH: MONOLITH]  <-- THE KEY DIFFERENCE
//   Steps 3..10 are in-process Java calls. ONE database transaction covers
//   stock + payment + order + items. Any RuntimeException => full rollback.
//   In microservices the same steps become 4 local transactions + a Saga.
// =============================================================================
@Service
public class OrderService {

    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderService(InventoryService inventoryService,
                        PaymentService paymentService,
                        NotificationService notificationService,
                        OrderRepository orderRepository,
                        CustomerRepository customerRepository) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    // [UML: Sequence] TRANSACTION FRAME starts here (the box around steps 3..10)
    @Transactional
    public OrderResponseDTO processOrder(OrderRequestDTO request) {

        // [UML: Sequence msg 3] in-process call: OrderService -> InventoryService
        // [UML: Sequence msg 4] returns "Stock Reserved"
        inventoryService.checkAndReserveStock(request.getItems());

        // [UML: Sequence msg 5] in-process call: OrderService -> PaymentService
        // [UML: Sequence msg 6] returns "Payment Success"
        // [ARCH] PaymentGateway is an External System, but the DB write is local.
        PaymentEntity payment = paymentService.processPayment(request.getPaymentInfo());

        // [UML: Class Diagram] Association OrderEntity -> CustomerEntity
        // [ARCH] Legal here: CustomerEntity is a real object in the SAME database.
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerId()));

        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setStatus("CONFIRMED");
        order.setTotalPrice(payment.getAmount());

        // [UML: Class Diagram] Composition OrderEntity (1) <>--> OrderItemEntity (1..*)
        // [ARCH] The items are real child objects; CascadeType.ALL persists them.
        List<OrderItemEntity> items = new ArrayList<>();
        for (OrderItemRequest line : request.getItems()) {
            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(line.getProductId());
            item.setQuantity(line.getQuantity());
            items.add(item);
        }
        order.setItems(items);

        // [UML: Sequence msg 7] OrderService -> OrderRepository : save(orderEntity)
        // [UML: Sequence msgs 8..10] handled inside Spring Data / Hibernate (INSERT + return)
        OrderEntity saved = orderRepository.save(order);

        // The payment row was created before the order row existed (diagram order).
        // Same JVM + same transaction => we can complete the FK before COMMIT.
        payment.setOrderId(saved.getId());

        // [UML: Package Diagram] NotificationService is in the package but is NOT
        // a lifeline on the Part-2 Sequence Diagram. Kept, marked as an extension.
        notificationService.sendOrderConfirmation(saved);

        // [UML: Sequence msg 11] OrderService -> OrderController : OrderResponseDTO
        return new OrderResponseDTO(saved.getId(), saved.getTotalPrice(), customer.getName());
    }
}
