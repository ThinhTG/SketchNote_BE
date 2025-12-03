package com.sketchnotes.project_service.dtos.request;

import com.sketchnotes.project_service.enums.PaperType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPaperRequest {
    @NotNull(message = "Paper type is required")
    private PaperType paperType;
    
    @NotBlank(message = "Name is required")
    private String name;
}
