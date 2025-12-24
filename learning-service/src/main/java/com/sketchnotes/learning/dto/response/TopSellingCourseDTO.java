package com.sketchnotes.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Top Selling Course statistics.
 * Used by AdminCourseStatsController to return course sales data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingCourseDTO {
    private Long courseId;
    private String title;
    private Long enrollmentCount;
}
