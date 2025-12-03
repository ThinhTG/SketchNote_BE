package com.sketchnotes.project_service.dtos.mapper;

import com.sketchnotes.project_service.dtos.request.PaperTemplateRequest;
import com.sketchnotes.project_service.dtos.response.PaperTemplateResponse;
import com.sketchnotes.project_service.entity.CategoryPaper;
import com.sketchnotes.project_service.entity.PaperTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaperTemplateMapper {
    
    public PaperTemplate toEntity(PaperTemplateRequest request, CategoryPaper categoryPaper) {
        return PaperTemplate.builder()
                .name(request.getName())
                .paperSize(request.getPaperSize())
                .categoryPaper(categoryPaper)
                .imageUrl(request.getImageUrl())
                .build();
    }
    
    public PaperTemplateResponse toResponse(PaperTemplate entity) {
        return PaperTemplateResponse.builder()
                .paperTemplateId(entity.getPaperTemplateId())
                .name(entity.getName())
                .paperSize(entity.getPaperSize())
                .categoryPaperId(entity.getCategoryPaper().getCategoryPaperId())
                .categoryPaperName(entity.getCategoryPaper().getName())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public void updateEntity(PaperTemplate entity, PaperTemplateRequest request, CategoryPaper categoryPaper) {
        entity.setName(request.getName());
        entity.setPaperSize(request.getPaperSize());
        entity.setCategoryPaper(categoryPaper);
        entity.setImageUrl(request.getImageUrl());
    }
}
