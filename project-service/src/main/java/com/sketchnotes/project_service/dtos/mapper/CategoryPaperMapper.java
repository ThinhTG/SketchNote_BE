package com.sketchnotes.project_service.dtos.mapper;

import com.sketchnotes.project_service.dtos.request.CategoryPaperRequest;
import com.sketchnotes.project_service.dtos.response.CategoryPaperResponse;
import com.sketchnotes.project_service.entity.CategoryPaper;
import org.springframework.stereotype.Component;

@Component
public class CategoryPaperMapper {
    
    public CategoryPaper toEntity(CategoryPaperRequest request) {
        return CategoryPaper.builder()
                .paperType(request.getPaperType())
                .name(request.getName())
                .build();
    }
    
    public CategoryPaperResponse toResponse(CategoryPaper entity) {
        return CategoryPaperResponse.builder()
                .categoryPaperId(entity.getCategoryPaperId())
                .paperType(entity.getPaperType())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public void updateEntity(CategoryPaper entity, CategoryPaperRequest request) {
        entity.setPaperType(request.getPaperType());
        entity.setName(request.getName());
    }
}
