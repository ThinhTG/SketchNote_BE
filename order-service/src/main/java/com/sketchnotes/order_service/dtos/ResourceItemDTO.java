package com.sketchnotes.order_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceItemDTO {
    private Long resourceItemId;
    private Integer itemIndex;
    private String itemUrl;
    private String imageUrl;
}
