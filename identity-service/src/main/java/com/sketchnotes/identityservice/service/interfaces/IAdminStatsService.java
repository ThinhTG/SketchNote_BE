package com.sketchnotes.identityservice.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for admin statistics operations.
 * Provides methods for retrieving user and course revenue statistics.
 */
public interface IAdminStatsService {
    
    /**
     * Get user statistics including total users, customers, and designers.
     * @return Map containing user statistics
     */
    Map<String, Long> getUserStats();
    
    /**
     * Get course revenue statistics grouped by specified period.
     * @param start Start date/time for the period
     * @param end End date/time for the period
     * @param groupBy Grouping period: "day", "month", or "year"
     * @return List of revenue data grouped by period
     */
    List<Map<String, Object>> getCourseRevenue(LocalDateTime start, LocalDateTime end, String groupBy);
}
