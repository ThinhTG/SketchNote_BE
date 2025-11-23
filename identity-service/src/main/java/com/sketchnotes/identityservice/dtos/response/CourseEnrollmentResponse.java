package com.sketchnotes.identityservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentResponse {
    
    private Long enrollmentId;
    
    private Long userId;
    
    private Long courseId;
    
    private LocalDateTime enrolledAt;
    
    private String status;  // ENROLLED, COMPLETED, IN_PROGRESS
    
    private BigDecimal progressPercent;  // 0-100
}
