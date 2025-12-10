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
    private String status;
    // Images associated with this template
    private java.util.List<ResourceImageDTO> images;
    private java.util.List<ResourceItemDTO> items;
    private DesignerInfoDTO designerInfo;
    
    // Indicates if the current user is the owner of this resource template
    // Used by frontend to disable "Buy" button for own resources
    private Boolean isOwner;
    
    // Statistics fields
    private Long purchaseCount;      // Số lượt mua
    private Long feedbackCount;      // Số feedback
    private Double averageRating;    // Số sao trung bình (1-5)

    // Aggregated resource rating (alias for averageRating for backward compatibility)
    private Double avgResourceRating;
}
