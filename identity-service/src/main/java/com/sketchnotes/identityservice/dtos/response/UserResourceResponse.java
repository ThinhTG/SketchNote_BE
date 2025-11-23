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
public class UserResourceResponse {
    
    private Long id;
    
    private Long userId;
    
    private Long resourceTemplateId;
    
    private Long orderId;
    
    private Boolean active;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
