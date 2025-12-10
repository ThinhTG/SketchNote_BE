package com.sketchnotes.order_service.dtos.designer;

import com.sketchnotes.order_service.dtos.DesignerInfoDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignerProductDTO {
    private Long resourceTemplateId;
    private Long designerId;
    private String name;
    private String description;
    private String type;
    private BigDecimal price;
    private String status;  // State Machine: PENDING_REVIEW, PUBLISHED, REJECTED, ARCHIVED, DELETED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Statistics
    private Long totalPurchases;     // Tổng số lượt mua từ tất cả versions
    private BigDecimal totalRevenue;  // Tổng doanh thu từ tất cả versions
    private Long averageRating;       // Xếp hạng trung bình
    
    // Version info
    private Long currentPublishedVersionId;                                                                                                                                                                                                                  
    private String currentVersionNumber;
    private List<ResourceTemplateVersionDTO> versions;
    
    private DesignerInfoDTO designerInfo;
    
    // Helper methods for status checks
    public boolean isArchived() {
        return "ARCHIVED".equals(status);
    }
    
    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }
    
    public boolean isPendingReview() {
        return "PENDING_REVIEW".equals(status);
    }
}
