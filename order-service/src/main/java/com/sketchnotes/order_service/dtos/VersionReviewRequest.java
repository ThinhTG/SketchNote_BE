package com.sketchnotes.order_service.dtos;

import lombok.Data;

@Data
public class VersionReviewRequest {
    private Boolean approve;      // true = approve, false = reject
    private String reviewComment; // required when reject
}
