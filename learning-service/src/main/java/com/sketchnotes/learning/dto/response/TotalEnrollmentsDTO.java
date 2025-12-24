package com.sketchnotes.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Total Enrollments statistics.
 * Used by AdminCourseStatsController to return total enrollment count.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalEnrollmentsDTO {
    private long totalEnrollments;
}
