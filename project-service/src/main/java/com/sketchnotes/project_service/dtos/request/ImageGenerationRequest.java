package com.sketchnotes.project_service.dtos.request;


import com.sketchnotes.project_service.enums.ImageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ImageGenerationRequest {

    private String prompt;
    private Integer width;
    private Integer height;
    private Boolean isIcon;
}