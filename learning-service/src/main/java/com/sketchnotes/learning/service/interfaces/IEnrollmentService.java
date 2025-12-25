package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.dto.EnrollmentDTO;

import java.util.List;
import java.util.Map;

/**
 * Service interface for enrollment operations.
 * Provides methods for managing course enrollments.
 */
public interface IEnrollmentService {
    
    /**
     * Enroll a user in a course.
     * @param courseId Course ID
     * @param userId User ID
     * @return Enrollment data
     */
    EnrollmentDTO enroll(long courseId, long userId);
    
    /**
     * Get the enrollment status of all courses for a user.
     * @param userId User ID
     * @return Map containing registered and not-registered courses
     */
    Map<String, List<CourseDTO>> getUserCourseStatus(long userId);
}
