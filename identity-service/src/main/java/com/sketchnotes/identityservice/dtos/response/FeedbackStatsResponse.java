package com.sketchnotes.identityservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackStatsResponse {
    
    private Long totalFeedbacks;
    
    private Double averageRating;
    
    private Map<Integer, Long> ratingDistribution;  // Key: star rating (1-5), Value: count
    
    private List<FeedbackResponse> feedbacks;
}
