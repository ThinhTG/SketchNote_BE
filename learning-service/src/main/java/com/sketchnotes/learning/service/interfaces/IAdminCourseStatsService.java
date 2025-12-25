package com.sketchnotes.learning.service.interfaces;

import java.util.List;
import java.util.Map;

/**
 * Service interface for admin course statistics.
 * Provides methods for retrieving course enrollment statistics.
 */
public interface IAdminCourseStatsService {
    
    /**
     * Get top selling courses by enrollment count.
     * @param limit Maximum number of courses to return
     * @return List of courses with their enrollment counts
     */
    List<Map<String, Object>> getTopSellingCourses(int limit);
    
    /**
     * Get total enrollment count across all courses.
     * @return Total number of enrollments
     */
    long getTotalEnrollments();
}
