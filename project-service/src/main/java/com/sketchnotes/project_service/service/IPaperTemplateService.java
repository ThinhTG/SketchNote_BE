package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.PaperTemplateRequest;
import com.sketchnotes.project_service.dtos.response.PaperTemplateResponse;
import com.sketchnotes.project_service.enums.PaperSize;
import com.sketchnotes.project_service.utils.PagedResponse;

public interface IPaperTemplateService {
    PaperTemplateResponse create(PaperTemplateRequest request);
    
    PaperTemplateResponse update(Long id, PaperTemplateRequest request);
    
    void delete(Long id);
    
    PaperTemplateResponse getById(Long id);
    
    PagedResponse<PaperTemplateResponse> getAll(Long categoryId, PaperSize paperSize, String keyword, int page, int size);
}
