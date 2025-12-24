package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.response.TopSellingCourseDTO;

import java.util.List;

/**
 * Service interface for Admin Course Statistics operations.
 * Controllers MUST depend on this interface, not the implementation.
 */
public interface IAdminCourseStatsService {
    
    /**
     * Get top selling courses by enrollment count
     * @param limit maximum number of courses to return
     * @return list of top selling courses with enrollment statistics
     */
    List<TopSellingCourseDTO> getTopSellingCourses(int limit);
    
    /**
     * Get total number of enrollments across all courses
     * @return total enrollment count
     */
    long getTotalEnrollments();
}
