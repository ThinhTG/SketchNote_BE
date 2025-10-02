package com.sketchnotes.order_service.mapper;

import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface OrderMapper {

    // RequestDTO -> Entity
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "orderDetails", source = "items")
    Order toEntity(OrderRequestDTO dto);

    // DetailDTO -> Entity
    @Mapping(target = "orderDetailId", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderDetail toEntity(OrderDetailDTO dto);

    // Entity -> ResponseDTO
    @Mapping(target = "items", source = "orderDetails")
    OrderResponseDTO toDto(Order order);

    OrderDetailDTO toDto(OrderDetail detail);

    List<OrderResponseDTO> toDtoList(List<Order> orders);
}
