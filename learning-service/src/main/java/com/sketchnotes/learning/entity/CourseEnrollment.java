package com.sketchnotes.learning.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// CourseEnrollment.java
@Entity
@Data
@Table(name = "course_enrollment")
public class CourseEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long enrollmentId;

    private long userId;  // từ Identity Service
    private LocalDateTime enrolledAt;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}