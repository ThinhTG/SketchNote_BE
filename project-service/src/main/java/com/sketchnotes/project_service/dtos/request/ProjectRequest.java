package com.sketchnotes.project_service.dtos.request;

import com.sketchnotes.project_service.enums.PaperSize;
import com.sketchnotes.project_service.enums.PaperType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProjectRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Description is required")
    private String description;
    private String imageUrl;
    @NotBlank(message = "Paper size is required")
    private String paperSize;
}
