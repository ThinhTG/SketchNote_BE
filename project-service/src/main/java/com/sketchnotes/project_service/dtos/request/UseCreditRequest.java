package com.sketchnotes.project_service.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho việc sử dụng credit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UseCreditRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Integer amount;
    
    private String description;
    
    private String referenceId;
}
