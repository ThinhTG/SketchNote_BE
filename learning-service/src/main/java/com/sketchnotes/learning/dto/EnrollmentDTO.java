package com.sketchnotes.learning.dto;

import com.sketchnotes.learning.dto.enums.EnrollmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentDTO {
    private Long enrollmentId;
    private Long userId;
    private Long courseId;
    private EnrollmentStatus status;
    private String paymentStatus;
    private LocalDateTime enrolledAt;
}

