package com.sketchnotes.learning.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "lesson")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonId;
    private String title;              // tiêu đề từng bài học
    private String description;
    private String videoUrl;
    private int duration;
    private String content;
    private int orderIndex;         // thứ tự sắp xếp các bài học trong 1 khóa
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnore
    private Course course;
}

