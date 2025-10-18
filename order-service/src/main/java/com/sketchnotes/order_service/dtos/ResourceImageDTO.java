package com.sketchnotes.order_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceImageDTO {
    private Long id;
    private String imageUrl;
    private Boolean isThumbnail;
}
