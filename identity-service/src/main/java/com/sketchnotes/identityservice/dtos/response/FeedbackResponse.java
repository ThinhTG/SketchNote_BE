package com.sketchnotes.identityservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    
    private Long id;
    
    private Long userId;
    
    private String userFullName;
    
    private String userAvatarUrl;
    
    private Long courseId;
    
    private Long resourceId;
    
    private Integer rating;
    
    private String comment;
    
    private Integer progressWhenSubmitted;
    
    private Boolean isEdited;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
