package com.sketchnotes.order_service.mapper;

import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // RequestDTO -> Entity
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "totalAmount", ignore = true) // Will be calculated in service
    @Mapping(target = "paymentStatus", constant = "PENDING")
    @Mapping(target = "orderStatus", constant = "PENDING")
    @Mapping(target = "invoiceNumber", ignore = true) // Will be generated in service
    @Mapping(target = "issueDate", ignore = true) // Will be set by @PrePersist
    @Mapping(target = "createdAt", ignore = true) // Will be set by @PrePersist
    @Mapping(target = "updatedAt", ignore = true) // Will be set by @PrePersist
    @Mapping(target = "orderDetails", source = "items", qualifiedByName = "mapOrderDetailRequests")
    Order toEntity(OrderRequestDTO dto);

    // OrderDetailRequestDTO -> OrderDetail Entity
    @Mapping(target = "orderDetailId", ignore = true)
    @Mapping(target = "order", ignore = true) // Sẽ set trong service
    @Mapping(target = "resourceTemplateId", source = "resourceTemplateId")
    @Mapping(target = "unitPrice", ignore = true) // Lấy từ template
    @Mapping(target = "discount", source = "discount", defaultValue = "0")
    @Mapping(target = "subtotalAmount", ignore = true) // Tính trong entity
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderDetail toEntity(OrderRequestDTO.OrderDetailRequestDTO dto);


    // Entity -> ResponseDTO
    @Mapping(target = "items", source = "orderDetails")
    OrderResponseDTO toDto(Order order);

    @Mapping(target = "templateName", ignore = true) // Will be populated from template
    @Mapping(target = "templateDescription", ignore = true) // Will be populated from template
    @Mapping(target = "templateType", ignore = true) // Will be populated from template
    OrderDetailDTO toDto(OrderDetail detail);

    List<OrderResponseDTO> toDtoList(List<Order> orders);

    // ResourceTemplate mappings
    ResourceTemplateDTO toDto(ResourceTemplate template);
    ResourceTemplate toEntity(ResourceTemplateDTO dto);
    List<ResourceTemplateDTO> toTemplateDtoList(List<ResourceTemplate> templates);
    
    // TemplateCreateUpdateDTO mappings
    ResourceTemplate toEntity(TemplateCreateUpdateDTO dto);
    TemplateCreateUpdateDTO toCreateUpdateDto(ResourceTemplate template);

    @Named("mapOrderDetailRequests")
    default List<OrderDetail> mapOrderDetailRequests(List<OrderRequestDTO.OrderDetailRequestDTO> items) {
        if (items == null) return null;
        return items.stream()
                .map(this::toEntity)
                .toList();
    }
}
