package com.sketchnotes.project_service.dtos.socket;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Message for collaborative drawing events
 * Represents a single stroke or drawing action from a user
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrawMessage {
    // User information
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    
    // Project information
    private Long projectId;
    
    // Drawing data
    private String drawingData; // JSON serialized drawing action (stroke, shape, etc.)
    private String actionType; // "STROKE", "ERASE", "CLEAR", "UNDO", "REDO"
    
    // Styling
    private String color;       // Hex color code (e.g., "#FF0000")
    private Float strokeWidth;  // Stroke width in pixels
    private String toolType;    // "PEN", "MARKER", "ERASER", "LINE", "CIRCLE", "RECT"
    
    // Timestamp
    private LocalDateTime timestamp;
    
    // Optional: For undo/redo coordination
    private Integer sequenceNumber; // Sequence of drawing actions
}
