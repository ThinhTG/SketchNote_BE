package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.dtos.OrderRequestDTO;
import com.sketchnotes.order_service.dtos.OrderResponseDTO;
import com.sketchnotes.order_service.entity.Order;
import com.sketchnotes.order_service.exception.OrderNotFoundException;
import com.sketchnotes.order_service.mapper.OrderMapper;
import com.sketchnotes.order_service.repository.OrderRepository;
import com.sketchnotes.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        Order order = orderMapper.toEntity(request);

        // xử lý gán quan hệ 2 chiều
        order.getOrderDetails().forEach(d -> d.setOrder(order));

        // tính tổng giá
        BigDecimal total = order.getOrderDetails().stream()
                .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }

    @Override
    public List<OrderResponseDTO> getAllOrdersByUser(Long userId) {
        return orderMapper.toDtoList(
                orderRepository.findAll().stream()
                        .filter(o -> o.getUserId().equals(userId))
                        .toList()
        );
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }
}
