package com.sketchnotes.order_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackStatsResponse {
    
    private Long totalFeedbacks;
    
    private Double averageRating;
}
