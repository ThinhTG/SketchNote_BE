package com.sketchnotes.identityservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentCheckResponse {
    private boolean isSafe;      // True if safe
    private int safetyScore;     // 0-100 (100 is safest)
    private String reason;       // Explanation for the score
}