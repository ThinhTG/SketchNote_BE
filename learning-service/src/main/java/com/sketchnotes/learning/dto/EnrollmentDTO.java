package com.sketchnotes.learning.dto;

import com.sketchnotes.learning.dto.enums.EnrollmentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class EnrollmentDTO {
    private Long enrollmentId;
    private Long userId;
    private Long courseId;
    private EnrollmentStatus status;
    private BigDecimal progressPercent; // percent from CourseEnrollment.progressPercent
    private CourseDTO course; // embedded course details for personalized view
    private LocalDateTime enrolledAt;
}

