package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.config.KafkaConfig;
import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.entity.*;
import com.sketchnotes.order_service.exception.OrderNotFoundException;
import com.sketchnotes.order_service.exception.ResourceTemplateNotFoundException;
import com.sketchnotes.order_service.mapper.OrderMapper;
import com.sketchnotes.order_service.repository.OrderRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sketchnotes.order_service.events.*;
import com.sketchnotes.order_service.repository.OrderEventLogRepository;
import lombok.extern.slf4j.Slf4j;


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


    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        // 1️⃣ Validate templates tồn tại và active
        for (OrderRequestDTO.OrderDetailRequestDTO item : request.getItems()) {
            resourceTemplateRepository.findByTemplateIdAndIsActiveTrue(item.getResourceTemplateId())
                    .orElseThrow(() -> new ResourceTemplateNotFoundException(
                            "Template not found or inactive: " + item.getResourceTemplateId()));
        }

        // 2️⃣ Map DTO -> Entity
        Order order = orderMapper.toEntity(request);

        // 3️⃣ Gắn giá template, discount và đảm bảo không null
        for (int i = 0; i < order.getOrderDetails().size(); i++) {
            OrderDetail detail = order.getOrderDetails().get(i);
            OrderRequestDTO.OrderDetailRequestDTO requestItem = request.getItems().get(i);

            ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndIsActiveTrue(
                            requestItem.getResourceTemplateId())
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
        order.setOrderStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return enrichOrderResponse(orderMapper.toDto(orderRepository.save(order)));
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
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByType(String type) {
        try {
            ResourceTemplate.TemplateType templateType = ResourceTemplate.TemplateType.valueOf(type.toUpperCase());
            return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByTypeAndIsActiveTrue(templateType));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid template type: " + type);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> searchTemplates(String keyword) {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.searchByKeyword(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceTemplateDTO getTemplateById(Long id) {
        ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        return orderMapper.toDto(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByPriceRange(minPrice, maxPrice));
    }

    // Helper method to enrich order response with template information
    private OrderResponseDTO enrichOrderResponse(OrderResponseDTO orderResponse) {
        if (orderResponse.getItems() != null) {
            for (OrderDetailDTO detail : orderResponse.getItems()) {
                try {
                    ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndIsActiveTrue(detail.getResourceTemplateId())
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
}
