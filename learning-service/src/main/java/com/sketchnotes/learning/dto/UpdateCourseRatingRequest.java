package com.sketchnotes.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để cập nhật rating của Course khi có feedback mới
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRatingRequest {
    private Double avgRating;      // Điểm trung bình mới
    private Integer ratingCount;   // Tổng số rating
}
