package com.sketchnotes.learning.dto;

import lombok.Data;
import com.sketchnotes.learning.dto.enums.ProgressStatus;
import java.time.LocalDateTime;

@Data
public class LessonDTO {
    private Long lessonId;
    private String title;
    private String content;
    private int orderIndex;
    private int duration;
    private String description;
    private String videoUrl;
    
    // Per-user progress fields (populated when returning personalized view)
    private ProgressStatus lessonProgressStatus;
    private Integer lastPosition;
    private Integer timeSpent;
    private LocalDateTime completedAt;
}
