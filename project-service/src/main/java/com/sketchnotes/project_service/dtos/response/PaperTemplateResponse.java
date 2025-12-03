package com.sketchnotes.project_service.dtos.response;

import com.sketchnotes.project_service.enums.PaperSize;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperTemplateResponse {
    private Long paperTemplateId;
    private String name;
    private PaperSize paperSize;
    private Long categoryPaperId;
    private String categoryPaperName;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
