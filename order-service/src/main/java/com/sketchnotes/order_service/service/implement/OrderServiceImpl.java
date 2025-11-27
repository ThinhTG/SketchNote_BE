package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.config.KafkaConfig;
import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.entity.*;
import com.sketchnotes.order_service.exception.OrderNotFoundException;
import com.sketchnotes.order_service.exception.ResourceTemplateNotFoundException;
import com.sketchnotes.order_service.mapper.OrderMapper;
import com.sketchnotes.order_service.repository.OrderDetailRepository;
import com.sketchnotes.order_service.repository.OrderRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.repository.UserResourceRepository;
import com.sketchnotes.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sketchnotes.order_service.events.*;
import com.sketchnotes.order_service.repository.OrderEventLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ResourceTemplateRepository resourceTemplateRepository;
    private final OrderMapper orderMapper;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;
    private final OrderEventLogRepository orderEventLogRepository;
    private final UserResourceRepository userResourceRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final IdentityClient identityClient;



    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        Long userId = request.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required to create order");
        }

        validateOrderDuplicates(userId, request.getItems());

        // 1️⃣ Validate templates tồn tại và active
    for (OrderRequestDTO.OrderDetailRequestDTO item : request.getItems()) {
        resourceTemplateRepository.findByTemplateIdAndStatus(item.getResourceTemplateId(), ResourceTemplate.TemplateStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceTemplateNotFoundException(
                "Template not found or inactive: " + item.getResourceTemplateId()));
    }

        // 2️⃣ Map DTO -> Entity
        Order order = orderMapper.toEntity(request);

        // 3️⃣ Gắn giá template, discount và đảm bảo không null
        for (int i = 0; i < order.getOrderDetails().size(); i++) {
            OrderDetail detail = order.getOrderDetails().get(i);
            OrderRequestDTO.OrderDetailRequestDTO requestItem = request.getItems().get(i);

        ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndStatus(
                requestItem.getResourceTemplateId(), ResourceTemplate.TemplateStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceTemplateNotFoundException(
                "Template not found: " + requestItem.getResourceTemplateId()));

            BigDecimal unitPrice = template.getPrice() != null ? template.getPrice() : BigDecimal.ZERO;
            BigDecimal discount = requestItem.getDiscount() != null ? requestItem.getDiscount() : BigDecimal.ZERO;


            detail.setUnitPrice(unitPrice);
            detail.setDiscount(discount);

            detail.setOrder(order);

            // 👇 Tính subtotalAmount ngay tại đây để đảm bảo không null
            BigDecimal subtotal = unitPrice.subtract(discount);
            detail.setSubtotalAmount(subtotal.max(BigDecimal.ZERO)); // tránh giá trị âm
        }

        // 4️⃣ Tính tổng toàn bộ chi tiết
        BigDecimal totalAmount = order.getOrderDetails().stream()
                .map(OrderDetail::getSubtotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);

        // 5️⃣ Sinh số hóa đơn
        order.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // 6️⃣ Lưu và trả về DTO
        Order saved = orderRepository.save(order);
        // 7️⃣ Publish OrderCreatedEvent -> PaymentService
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(saved.getOrderId())
                .userId(saved.getUserId())
                .totalAmount(saved.getTotalAmount())
                .items(saved.getOrderDetails().stream()
                        .map(d -> new OrderCreatedEvent.OrderItemEvent(
                                d.getResourceTemplateId(),
                                d.getUnitPrice(),
                                d.getDiscount()))
                        .toList())
                .build();

// Log event
        try {
            String payload = objectMapper.writeValueAsString(event);
            orderEventLogRepository.save(OrderEventLog.builder()
                    .orderId(saved.getOrderId())
                    .eventType("ORDER_CREATED")
                    .payload(payload)
                    .build());
        } catch (Exception e) {
            log.error("❌ Failed to serialize OrderCreatedEvent for order {}: {}", saved.getOrderId(), e.getMessage());
        }


// Gửi event qua Kafka (binding name = orderCreated-out-0)
        KafkaConfig.sendEvent(streamBridge, "orderCreated-out-0", event);
        return enrichOrderResponse(orderMapper.toDto(saved));
    }

    private void validateOrderDuplicates(Long userId, List<OrderRequestDTO.OrderDetailRequestDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one template item");
        }

        List<String> orderStatuses = List.of("PENDING", "SUCCESS");
        List<String> paymentStatuses = List.of("PENDING", "PAID");

        for (OrderRequestDTO.OrderDetailRequestDTO item : items) {
            Long templateId = item.getResourceTemplateId();
            if (templateId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template id is required for each order item");
            }

            // ✅ Validate: User cannot buy their own template
            ResourceTemplate template = resourceTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            String.format("Template %d not found", templateId)));
            
            if (template.getDesignerId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("You cannot purchase your own template (ID: %d)", templateId));
            }

            if (userResourceRepository.existsByUserIdAndResourceTemplateIdAndActiveTrue(userId, templateId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        String.format("You already own template %d", templateId));
            }

            if (orderDetailRepository.existsByUserAndTemplateWithStatuses(userId, templateId, orderStatuses, paymentStatuses)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        String.format("You already have a pending order for template %d", templateId));
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return enrichOrderResponse(orderMapper.toDto(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrdersByUser(Long userId) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getUserId().equals(userId))
                .toList();
        return orders.stream()
                .map(orderMapper::toDto)
                .map(this::enrichOrderResponse)
                .toList();
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        
        String oldStatus = order.getOrderStatus();
        order.setOrderStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        
        // Send notifications if order becomes SUCCESS and payment is PAID
        if ("SUCCESS".equals(status) && !"SUCCESS".equals(oldStatus) && "PAID".equals(order.getPaymentStatus())) {
            log.info("Order {} status changed to SUCCESS with PAID payment, sending notifications", id);
            sendPaymentSuccessNotifications(id);
        }
        
        return enrichOrderResponse(orderMapper.toDto(savedOrder));
    }

    @Override
    public OrderResponseDTO updatePaymentStatus(Long id, String paymentStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return enrichOrderResponse(orderMapper.toDto(orderRepository.save(order)));
    }

    // Template management methods
    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getAllTemplates() {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByType(String type) {
        try {
            ResourceTemplate.TemplateType templateType = ResourceTemplate.TemplateType.valueOf(type.toUpperCase());
            return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByTypeAndStatus(templateType, ResourceTemplate.TemplateStatus.PUBLISHED));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid template type: " + type);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> searchTemplates(String keyword) {
    return orderMapper.toTemplateDtoList(resourceTemplateRepository.searchByKeyword(keyword, ResourceTemplate.TemplateStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceTemplateDTO getTemplateById(Long id) {
    ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndStatus(id, ResourceTemplate.TemplateStatus.PUBLISHED)
        .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        return orderMapper.toDto(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
    return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByPriceRange(minPrice, maxPrice, ResourceTemplate.TemplateStatus.PUBLISHED));
    }

    // Helper method to enrich order response with template information
    private OrderResponseDTO enrichOrderResponse(OrderResponseDTO orderResponse) {
        if (orderResponse.getItems() != null) {
            for (OrderDetailDTO detail : orderResponse.getItems()) {
                try {
            ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndStatus(detail.getResourceTemplateId(), ResourceTemplate.TemplateStatus.PUBLISHED)
                .orElse(null);
                    if (template != null) {
                        detail.setTemplateName(template.getName());
                        detail.setTemplateDescription(template.getDescription());
                        detail.setTemplateType(template.getType().toString());
                    }
                } catch (Exception e) {
                    // Log error but don't fail the request
                    detail.setTemplateName("Template not found");
                    detail.setTemplateDescription("Template information unavailable");
                    detail.setTemplateType("UNKNOWN");
                }
            }
        }
        return orderResponse;
    }
    
    /**
     * Send notifications to buyer and designers when payment is successful.
     */
    private void sendPaymentSuccessNotifications(Long orderId) {
        try {
            OrderResponseDTO order = getOrderById(orderId);
            
            // Notification to buyer
            String buyerMessage = buildBuyerNotificationMessage(order);
            CreateNotificationRequest buyerNotification = CreateNotificationRequest.builder()
                    .userId(order.getUserId())
                    .title("Thanh toán thành công")
                    .message(buyerMessage)
                    .type("PURCHASE_CONFIRM")
                    .orderId(order.getOrderId())
                    .build();
            
            identityClient.createNotification(buyerNotification);
            log.info("Sent purchase confirmation notification to user {}", order.getUserId());
            
            // Notifications to designers (one per unique designer)
            order.getItems().stream()
                    .collect(java.util.stream.Collectors.groupingBy(OrderDetailDTO::getResourceTemplateId))
                    .forEach((templateId, items) -> {
                        try {
                            ResourceTemplate template = resourceTemplateRepository
                                    .findById(templateId)
                                    .orElse(null);
                            
                            if (template != null) {
                                String designerMessage = String.format(
                                        "Tài nguyên '%s' của bạn đã được mua. Mã đơn: %s",
                                        template.getName(),
                                        order.getInvoiceNumber()
                                );
                                
                                CreateNotificationRequest designerNotification = CreateNotificationRequest.builder()
                                        .userId(template.getDesignerId())
                                        .title("Tài nguyên của bạn đã được mua")
                                        .message(designerMessage)
                                        .type("PURCHASE")
                                        .orderId(order.getOrderId())
                                        .resourceItemId(templateId)
                                        .build();
                                
                                identityClient.createNotification(designerNotification);
                                log.info("Sent purchase notification to designer {} for template {}", 
                                        template.getDesignerId(), templateId);
                            }
                        } catch (Exception e) {
                            log.error("Failed to send notification to designer for template {}: {}", 
                                    templateId, e.getMessage());
                        }
                    });
            
        } catch (Exception e) {
            log.error("Failed to send payment success notifications for order {}: {}", 
                    orderId, e.getMessage(), e);
            // Don't throw exception - notifications are not critical
        }
    }
    
    /**
     * Build a detailed message for the buyer notification.
     */
    private String buildBuyerNotificationMessage(OrderResponseDTO order) {
        String itemsList = order.getItems().stream()
                .map(item -> item.getTemplateName())
                .collect(java.util.stream.Collectors.joining(", "));
        
        return String.format(
                "Bạn đã mua thành công: %s. Mã đơn: %s. Tổng tiền: %s VND",
                itemsList,
                order.getInvoiceNumber(),
                order.getTotalAmount()
        );
    }
}
