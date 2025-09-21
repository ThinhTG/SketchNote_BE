package com.sketchnotes.learning.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseDTO {
    private String title;
    private String subtitle;
    private double price;
    private int studentCount;
    private String description;
    private String category;
    private List<LessonDTO> lessons;
}
