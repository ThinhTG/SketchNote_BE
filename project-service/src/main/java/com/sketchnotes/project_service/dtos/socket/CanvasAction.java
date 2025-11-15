package com.sketchnotes.project_service.dtos.socket;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanvasAction {
    private String type;
    private Long projectId;
    private Long userId;
    private Map<String, Object> payload;
}
