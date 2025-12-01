package com.sketchnotes.project_service.dtos.request;


import com.sketchnotes.project_service.enums.ImageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ImageGenerationRequest {
    @Length(max = 1000, min = 1, message = "Prompt length must not exceed 1000 characters and must not be empty.")
    private String prompt;
    private Integer width;
    private Integer height;
    private Boolean isIcon;
}