package com.sketchnotes.project_service.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePageRequest {
    private Integer pageNumber;
    private String strokeUrl;
}
