package com.sketchnotes.order_service.dtos.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse {
    private Long projectId;
    private Long pageId;
    private Integer pageNumber;
    private String strokeUrl;
}