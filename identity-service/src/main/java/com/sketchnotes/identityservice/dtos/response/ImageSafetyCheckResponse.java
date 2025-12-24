package com.sketchnotes.identityservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSafetyCheckResponse {
    
    private String imageUrl;
    private boolean isSafe;
    private SafeSearchDetails safeSearchDetails;
    private String summary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafeSearchDetails {
        private String adult;
        private String violence;
        private String racy;
        private String medical;
        private String spoof;
    }
}
