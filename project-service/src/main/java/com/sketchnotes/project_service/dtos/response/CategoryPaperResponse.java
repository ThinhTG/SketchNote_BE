package com.sketchnotes.project_service.dtos.response;

import com.sketchnotes.project_service.enums.PaperType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPaperResponse {
    private Long categoryPaperId;
    @Enumerated(EnumType.STRING)
    private PaperType paperType;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
