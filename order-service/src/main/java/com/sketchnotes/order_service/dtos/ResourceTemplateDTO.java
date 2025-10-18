package com.sketchnotes.order_service.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceTemplateDTO {
    private Long resourceTemplateId;
    private Long designerId;
    private String name;
    private String description;
    private String type;
    private BigDecimal price;
    private LocalDate expiredTime;
    private LocalDate releaseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    // Images associated with this template
    private java.util.List<ResourceImageDTO> images;
}
