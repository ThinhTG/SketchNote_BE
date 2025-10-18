package com.sketchnotes.order_service.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateCreateUpdateDTO {
    private Long templateId;
    private Long designerId;
    private String name;
    private String description;
    private String type;
    private BigDecimal price;
    private LocalDate expiredTime;
    private LocalDate releaseDate;
    private java.util.List<ResourceImageDTO> images;
    private java.util.List<ResourceItemDTO> items;
}
