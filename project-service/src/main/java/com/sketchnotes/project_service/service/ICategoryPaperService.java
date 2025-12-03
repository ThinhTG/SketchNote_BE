package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.CategoryPaperRequest;
import com.sketchnotes.project_service.dtos.response.CategoryPaperResponse;
import com.sketchnotes.project_service.enums.PaperType;
import com.sketchnotes.project_service.utils.PagedResponse;

public interface ICategoryPaperService {
    CategoryPaperResponse create(CategoryPaperRequest request);
    
    CategoryPaperResponse update(Long id, CategoryPaperRequest request);
    
    void delete(Long id);
    
    CategoryPaperResponse getById(Long id);
    
    PagedResponse<CategoryPaperResponse> getAll(PaperType paperType, String keyword, int page, int size);
}
