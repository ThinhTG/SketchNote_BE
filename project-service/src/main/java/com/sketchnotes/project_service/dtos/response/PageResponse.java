package com.sketchnotes.project_service.dtos.response;

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
    private String snapshotUrl;
}