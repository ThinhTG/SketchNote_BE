package com.sketchnotes.project_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private Long pageId;
    private Integer pageNumber;
    private String strokeUrl;
}