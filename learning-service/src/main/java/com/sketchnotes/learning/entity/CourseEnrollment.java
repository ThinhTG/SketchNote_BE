package com.sketchnotes.learning.entity;

import com.sketchnotes.learning.dto.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

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

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;  // PENDING, ENROLLED, COMPLETED, CANCELLED
    
    private String paymentStatus;  // PENDING_PAYMENT, PAYMENT_SUCCESS, PAYMENT_FAILED
    
    @Column(name = "progress_percent", precision = 5, scale = 2)
    private BigDecimal progressPercent = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}