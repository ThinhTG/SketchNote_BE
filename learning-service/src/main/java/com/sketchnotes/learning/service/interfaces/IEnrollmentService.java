package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.dto.EnrollmentDTO;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Enrollment operations.
 * Controllers MUST depend on this interface, not the implementation.
 */
public interface IEnrollmentService {
    
    EnrollmentDTO enroll(long courseId, long userId);
    
    Map<String, List<CourseDTO>> getUserCourseStatus(long userId);
    
    List<EnrollmentDTO> getEnrollmentsByUser(Long userId);
    
    EnrollmentDTO getEnrollmentByUserAndCourse(Long userId, Long courseId);
}
