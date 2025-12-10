package com.sketchnotes.learning.dto;

import com.sketchnotes.learning.dto.enums.SketchNoteCategory;
import lombok.Data;

import java.util.List;

@Data
public class CourseDTO {
    private Long courseId;
    private String title;
    private String subtitle;
    private double price;
    private String imageUrl;
    private int totalDuration;
    private int studentCount;
    private String description;
    private SketchNoteCategory category;
    private Double avgRating;
    private Integer ratingCount;
    private List<LessonDTO> lessons;
}
