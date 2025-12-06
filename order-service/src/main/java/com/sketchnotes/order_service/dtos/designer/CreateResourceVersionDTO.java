package com.sketchnotes.order_service.dtos.designer;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.sketchnotes.order_service.dtos.ResourceImageDTO;
import com.sketchnotes.order_service.dtos.ResourceItemDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateResourceVersionDTO {
    // Loại source để tạo version mới
    private String sourceType; // "UPLOAD" hoặc "PROJECT"
    private Long projectId; // Nếu sourceType = "PROJECT"
    
    // Metadata mới
    private String name;
    private String description;
    private String type;
    private BigDecimal price;
    private LocalDate expiredTime;
    private LocalDate releaseDate;
    
    // Images and items
    private List<ResourceImageDTO> images;
    private List<ResourceItemDTO> items;
}
