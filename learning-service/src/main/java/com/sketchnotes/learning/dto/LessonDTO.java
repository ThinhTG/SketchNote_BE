package com.sketchnotes.learning.dto;

import lombok.Data;

@Data
public class LessonDTO {
    private Long lessonId;
    private String title;
    private String content;
    private int orderIndex;
    private int duration;
    private String description;
    private String videoUrl;
}
