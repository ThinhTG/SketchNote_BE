package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.CourseDTO;

import java.util.List;

/**
 * Service interface for course operations.
 * Provides methods for managing courses and their related operations.
 */
public interface ICourseService {
    
    /**
     * Get all courses with their lessons.
     * @return List of all courses
     */
    List<CourseDTO> getAllCourses();
    
    /**
     * Create a new course.
     * @param dto Course data transfer object
     * @return Created course
     */
    CourseDTO createCourse(CourseDTO dto);
    
    /**
     * Get a course by its ID.
     * @param id Course ID
     * @return Course data
     */
    CourseDTO getCourseById(Long id);
    
    /**
     * Update an existing course.
     * @param id Course ID
     * @param dto Updated course data
     * @return Updated course
     */
    CourseDTO updateCourse(Long id, CourseDTO dto);
    
    /**
     * Delete a course by ID.
     * @param id Course ID
     */
    void deleteCourse(Long id);
    
    /**
     * Get all courses enrolled by a specific user.
     * @param userId User ID
     * @return List of enrolled courses
     */
    List<CourseDTO> getEnrolledCourses(Long userId);
}
