package com.sketchnotes.order_service.dtos;

import com.sketchnotes.order_service.dtos.designer.ResourceTemplateVersionDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for purchased templates that includes both the purchased version 
 * and the current (latest) version for user access
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasedTemplateDTO {
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
    
    // Version information
    private Long purchasedVersionId;        // Version ID that user originally purchased
    private String purchasedVersionNumber;  // Version number user purchased (e.g., "1.0")
    private Long currentVersionId;          // Version ID user is currently using (can be upgraded)
    private String currentVersionNumber;    // Version number user is currently using
    private Long latestVersionId;           // Latest published version ID
    private String latestVersionNumber;     // Latest version number (e.g., "2.0")
    
    // Flag to indicate if there's a newer version available for upgrade
    private boolean hasNewerVersion;
    
    // Available versions for the user (purchased version + all newer versions)
    private List<ResourceTemplateVersionDTO> availableVersions;
    
    // Images from current version (for display)
    private List<ResourceImageDTO> images;
    
    // Items from current version (for download/use)
    private List<ResourceItemDTO> items;
}
