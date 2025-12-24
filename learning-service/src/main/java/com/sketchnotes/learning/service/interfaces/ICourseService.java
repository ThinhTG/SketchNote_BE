package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.CourseDTO;

import java.util.List;

/**
 * Service interface for Course operations.
 * Controllers MUST depend on this interface, not the implementation.
 */
public interface ICourseService {
    
    List<CourseDTO> getAllCourses();
    
    CourseDTO getCourseById(Long id);
    
    CourseDTO createCourse(CourseDTO dto);
    
    CourseDTO updateCourse(Long id, CourseDTO dto);
    
    void deleteCourse(Long id);
    
    List<CourseDTO> getEnrolledCourses(Long userId);
    
    List<CourseDTO> getNotEnrolledCourses(Long userId);
    
    void updateCourseRating(Long courseId, Double avgRating, Integer ratingCount);
    
    CourseDTO getCourseRating(Long courseId);
}
