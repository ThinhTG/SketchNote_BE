package com.sketchnotes.order_service.service.implement;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ResourceTemplateRepository resourceTemplateRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        // Validate templates exist and are active
        for (OrderRequestDTO.OrderDetailRequestDTO item : request.getItems()) {
            ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndIsActiveTrue(item.getResourceTemplateId())
                    .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found or inactive: " + item.getResourceTemplateId()));
        }

        Order order = orderMapper.toEntity(request);
        
        // Set order details with template prices
        for (int i = 0; i < order.getOrderDetails().size(); i++) {
            OrderDetail detail = order.getOrderDetails().get(i);
            OrderRequestDTO.OrderDetailRequestDTO requestItem = request.getItems().get(i);
            
            ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndIsActiveTrue(requestItem.getResourceTemplateId())
                    .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found: " + requestItem.getResourceTemplateId()));
            
            detail.setUnitPrice(template.getPrice());
            detail.setDiscount(requestItem.getDiscount() != null ? requestItem.getDiscount() : BigDecimal.ZERO);
            detail.setOrder(order);
        }

        // Calculate total amount
        BigDecimal totalAmount = order.getOrderDetails().stream()
                .map(OrderDetail::getSubtotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // Generate invoice number
        order.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        Order saved = orderRepository.save(order);
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
