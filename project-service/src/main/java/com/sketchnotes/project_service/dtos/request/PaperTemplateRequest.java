package com.sketchnotes.project_service.dtos.request;

import com.sketchnotes.project_service.enums.PaperSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperTemplateRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Paper size is required")
    private PaperSize paperSize;
    
    @NotNull(message = "Category paper ID is required")
    private Long categoryPaperId;
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
}
