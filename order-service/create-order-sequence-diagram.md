# Create Order Sequence Diagram

## Mermaid Diagram for Draw.io

```mermaid
sequenceDiagram
    autonumber
    
    participant Client
    participant API Gateway
    participant OrderController
    participant IdentityClient
    participant IdentityService
    participant OrderServiceImpl
    participant OrderMapper
    participant UserResourceRepository
    participant OrderDetailRepository
    participant ResourceTemplateRepository
    participant OrderRepository
    participant OrderEventLogRepository
    participant StreamBridge as Kafka StreamBridge
    participant PaymentService as Payment Service (Kafka Consumer)
    
    %% ===== PHASE 1: Create Order Request =====
    Client->>API Gateway: POST /api/orders (OrderRequestDTO)
    API Gateway->>OrderController: createOrder(OrderRequestDTO)
    
    %% Get current user from Identity Service
    OrderController->>IdentityClient: getCurrentUser()
    IdentityClient->>IdentityService: GET /api/users/me
    IdentityService-->>IdentityClient: ApiResponse<UserResponse>
    IdentityClient-->>OrderController: UserResponse (userId)
    
    Note over OrderController: Set userId from token to DTO
    OrderController->>OrderServiceImpl: createOrder(OrderRequestDTO)
    
    %% ===== PHASE 2: Validation =====
    Note over OrderServiceImpl: Validate userId is not null
    
    alt userId is null
        OrderServiceImpl-->>OrderController: ResponseStatusException (BAD_REQUEST)
        OrderController-->>API Gateway: 400 Bad Request
        API Gateway-->>Client: Error Response
    end
    
    %% Validate duplicates
    OrderServiceImpl->>OrderServiceImpl: validateOrderDuplicates(userId, items)
    
    loop For each item in order
        OrderServiceImpl->>ResourceTemplateRepository: findById(templateId)
        ResourceTemplateRepository-->>OrderServiceImpl: ResourceTemplate
        
        Note over OrderServiceImpl: Check if user is buying their own template
        alt User buying own template
            OrderServiceImpl-->>OrderController: ResponseStatusException (FORBIDDEN)
        end
        
        OrderServiceImpl->>UserResourceRepository: existsByUserIdAndResourceTemplateIdAndActiveTrue()
        UserResourceRepository-->>OrderServiceImpl: boolean
        
        alt User already owns template
            OrderServiceImpl-->>OrderController: ResponseStatusException (CONFLICT)
        end
        
        OrderServiceImpl->>OrderDetailRepository: existsByUserAndTemplateWithStatuses()
        OrderDetailRepository-->>OrderServiceImpl: boolean
        
        alt Pending order exists
            OrderServiceImpl-->>OrderController: ResponseStatusException (CONFLICT)
        end
    end
    
    %% ===== PHASE 3: Validate Templates Exist and Active =====
    loop For each item
        OrderServiceImpl->>ResourceTemplateRepository: findByTemplateIdAndStatus(templateId, PUBLISHED)
        ResourceTemplateRepository-->>OrderServiceImpl: ResourceTemplate
        
        alt Template not found or inactive
            OrderServiceImpl-->>OrderController: ResourceTemplateNotFoundException
        end
    end
    
    %% ===== PHASE 4: Map DTO to Entity =====
    OrderServiceImpl->>OrderMapper: toEntity(OrderRequestDTO)
    OrderMapper-->>OrderServiceImpl: Order (with OrderDetails)
    
    %% ===== PHASE 5: Calculate Prices =====
    loop For each OrderDetail
        OrderServiceImpl->>ResourceTemplateRepository: findByTemplateIdAndStatus(templateId, PUBLISHED)
        ResourceTemplateRepository-->>OrderServiceImpl: ResourceTemplate (with price)
        
        Note over OrderServiceImpl: Set unitPrice from template<br/>Set discount from request<br/>Calculate subtotalAmount = unitPrice - discount<br/>Set order reference
    end
    
    Note over OrderServiceImpl: Calculate totalAmount = SUM(subtotalAmounts)
    Note over OrderServiceImpl: Generate invoiceNumber = "INV-" + UUID
    
    %% ===== PHASE 6: Save Order =====
    OrderServiceImpl->>OrderRepository: save(Order)
    Note over OrderRepository: @PrePersist sets:<br/>- createdAt<br/>- updatedAt<br/>- issueDate<br/>- paymentStatus = "PENDING"<br/>- orderStatus = "PENDING"
    OrderRepository-->>OrderServiceImpl: Order (saved with ID)
    
    %% ===== PHASE 7: Log Event =====
    OrderServiceImpl->>OrderServiceImpl: Build OrderCreatedEvent
    OrderServiceImpl->>OrderEventLogRepository: save(OrderEventLog)
    OrderEventLogRepository-->>OrderServiceImpl: OrderEventLog
    
    %% ===== PHASE 8: Publish Kafka Event =====
    OrderServiceImpl->>StreamBridge: send("orderCreated-out-0", OrderCreatedEvent)
    Note over StreamBridge: Async message to Kafka topic
    StreamBridge-->>OrderServiceImpl: void
    
    %% ===== PHASE 9: Enrich Response =====
    OrderServiceImpl->>OrderMapper: toDto(Order)
    OrderMapper-->>OrderServiceImpl: OrderResponseDTO
    
    OrderServiceImpl->>OrderServiceImpl: enrichOrderResponse(OrderResponseDTO)
    
    loop For each OrderDetailDTO
        OrderServiceImpl->>ResourceTemplateRepository: findByTemplateIdAndStatus()
        ResourceTemplateRepository-->>OrderServiceImpl: ResourceTemplate
        Note over OrderServiceImpl: Set templateName<br/>Set templateDescription<br/>Set templateType
    end
    
    %% ===== PHASE 10: Return Response =====
    OrderServiceImpl-->>OrderController: OrderResponseDTO
    OrderController-->>API Gateway: ApiResponse.success(OrderResponseDTO)
    API Gateway-->>Client: 200 OK (OrderResponseDTO)
    
    %% ===== ASYNC: Payment Service Processing =====
    Note over StreamBridge, PaymentService: Async Kafka Processing
    StreamBridge-)PaymentService: OrderCreatedEvent (via Kafka)
    Note over PaymentService: Payment Service creates<br/>payment link and waits<br/>for payment callback
```

## Simplified Version (Without Internal Details)

```mermaid
sequenceDiagram
    autonumber
    
    participant Client
    participant API Gateway
    participant Order Service
    participant Identity Service
    participant Database
    participant Kafka
    participant Payment Service
    
    Client->>API Gateway: POST /api/orders
    API Gateway->>Order Service: createOrder(OrderRequestDTO)
    
    Order Service->>Identity Service: Get Current User
    Identity Service-->>Order Service: UserResponse
    
    Order Service->>Database: Validate Templates Exist
    Database-->>Order Service: Templates
    
    Order Service->>Database: Check User Doesn't Own Templates
    Database-->>Order Service: Validation OK
    
    Order Service->>Database: Check No Pending Orders
    Database-->>Order Service: Validation OK
    
    Note over Order Service: Calculate prices and totals
    
    Order Service->>Database: Save Order (PENDING)
    Database-->>Order Service: Order Saved
    
    Order Service->>Database: Log Event
    Database-->>Order Service: Event Logged
    
    Order Service->>Kafka: Publish OrderCreatedEvent
    
    Order Service-->>API Gateway: OrderResponseDTO
    API Gateway-->>Client: 200 OK
    
    Note over Kafka, Payment Service: Async Processing
    Kafka-)Payment Service: OrderCreatedEvent
    Note over Payment Service: Create Payment Link
```

## Data Flow

### OrderRequestDTO
```json
{
    "userId": null,  // Set by controller from JWT token
    "subscriptionId": null,  // Optional
    "items": [
        {
            "resourceTemplateId": 1,
            "discount": 0.00  // Optional
        }
    ]
}
```

### OrderCreatedEvent (Kafka)
```json
{
    "orderId": 123,
    "userId": 456,
    "totalAmount": 100.00,
    "items": [
        {
            "resourceTemplateId": 1,
            "price": 100.00,
            "discount": 0.00
        }
    ]
}
```

### OrderResponseDTO
```json
{
    "orderId": 123,
    "userId": 456,
    "totalAmount": 100.00,
    "paymentStatus": "PENDING",
    "orderStatus": "PENDING",
    "invoiceNumber": "INV-A1B2C3D4",
    "issueDate": "2025-12-02T10:00:00",
    "items": [
        {
            "orderDetailId": 1,
            "resourceTemplateId": 1,
            "unitPrice": 100.00,
            "discount": 0.00,
            "subtotalAmount": 100.00,
            "templateName": "Template Name",
            "templateDescription": "Description",
            "templateType": "STICKER"
        }
    ]
}
```

## Key Components

| Component | Responsibility |
|-----------|---------------|
| OrderController | Handle HTTP request, get user from token |
| IdentityClient | Feign client to Identity Service |
| OrderServiceImpl | Business logic, validation, event publishing |
| OrderMapper | DTO <-> Entity conversion |
| ResourceTemplateRepository | Access template data |
| UserResourceRepository | Check user ownership |
| OrderDetailRepository | Check pending orders |
| OrderRepository | Persist orders |
| StreamBridge | Publish Kafka events |
| Payment Service | Handle payment asynchronously |

## Validation Rules

1. **User ID Required**: Must have authenticated user
2. **Cannot Buy Own Template**: Designer cannot purchase their own template
3. **No Duplicate Ownership**: User cannot buy template they already own
4. **No Pending Orders**: User cannot have multiple pending orders for same template
5. **Template Must Be Active**: Template status must be PUBLISHED
