package com.sketchnotes.project_service.dtos.socket;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanvasAction {
    /**
     * Loại action, ví dụ:
     * DRAW, ERASE, SHAPE_CREATE, MOVE, RESIZE, TEXT_ADD, TEXT_EDIT
     */
    private String type;

    /**
     * Tool đang dùng, ví dụ: pen, rectangle, circle, eraser, select, text
     */
    private String tool;

    /**
     * Project / room id
     */
    private Long projectId;

    /**
     * Id user thực hiện action
     */
    private Long userId;

    /**
     * Payload chứa dữ liệu tool:
     * - Pen: points, color, strokeWidth
     * - Shape: elementId, x, y, width, height, fill, stroke
     * - Move: elementId, dx, dy
     * - Erase: elementId
     */
    private Map<String, Object> payload;
}

