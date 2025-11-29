package com.sketchnotes.project_service.dtos.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationResponse {
    private String prompt;
    private String fileName;
    private List<String> imageUrls;
    private Long generationTime;
}