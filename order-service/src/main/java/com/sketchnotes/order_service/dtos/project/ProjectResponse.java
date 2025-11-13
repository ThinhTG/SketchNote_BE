package com.sketchnotes.order_service.dtos.project;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long projectId;
    private String name;
    private String description;
    private String imageUrl;
    private Long ownerId;
    private List<PageResponse> pages;
}