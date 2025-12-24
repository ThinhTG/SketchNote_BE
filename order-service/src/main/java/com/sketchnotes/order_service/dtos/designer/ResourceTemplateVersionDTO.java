package com.sketchnotes.order_service.dtos.designer;

import com.sketchnotes.order_service.dtos.ResourceImageDTO;
import com.sketchnotes.order_service.dtos.ResourceItemDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceTemplateVersionDTO {
    private Long versionId;
    private Long templateId;
    private String versionNumber;
    private String name;
    private String description;
    private String type;
    private BigDecimal price;
    private LocalDate releaseDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewComment;
    private List<ResourceImageDTO> images;
    private List<ResourceItemDTO> items;
    
    // Statistics
    private Long purchaseCount;
    private Long feedbackCount;
    private Double averageRating;
}
