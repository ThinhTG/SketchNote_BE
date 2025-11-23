package com.sketchnotes.identityservice.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequest {
    
    private Long courseId;
    
    private Long resourceId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;
    
    // Custom validation to ensure exactly one of courseId or resourceId is provided
    @AssertTrue(message = "Must provide exactly one of courseId or resourceId")
    public boolean isValidTarget() {
        return (courseId != null && resourceId == null) || (courseId == null && resourceId != null);
    }
}
