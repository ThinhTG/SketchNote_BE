package com.sketchnotes.learning.entity;

import com.sketchnotes.learning.dto.enums.EnrollmentStatus;
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

    private String status;  // PENDING_PAYMENT, PAYMENT_FAILED, ENROLLED
    private String failureReason;  // Lý do thất bại nếu có
    
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}