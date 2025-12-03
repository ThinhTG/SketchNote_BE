package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.dtos.mapper.PaperTemplateMapper;
import com.sketchnotes.project_service.dtos.request.PaperTemplateRequest;
import com.sketchnotes.project_service.dtos.response.PaperTemplateResponse;
import com.sketchnotes.project_service.entity.CategoryPaper;
import com.sketchnotes.project_service.entity.PaperTemplate;
import com.sketchnotes.project_service.enums.PaperSize;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.repository.ICategoryPaperRepository;
import com.sketchnotes.project_service.repository.IPaperTemplateRepository;
import com.sketchnotes.project_service.service.IPaperTemplateService;
import com.sketchnotes.project_service.utils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperTemplateService implements IPaperTemplateService {
    
    private final IPaperTemplateRepository paperTemplateRepository;
    private final ICategoryPaperRepository categoryPaperRepository;
    private final PaperTemplateMapper paperTemplateMapper;
    
    @Override
    @Transactional
    public PaperTemplateResponse create(PaperTemplateRequest request) {
        // Check if name already exists
        if (paperTemplateRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new AppException(ErrorCode.PAPER_TEMPLATE_NAME_EXISTED);
        }
        
        // Get category paper
        CategoryPaper categoryPaper = categoryPaperRepository
                .findByCategoryPaperIdAndDeletedAtIsNull(request.getCategoryPaperId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_PAPER_NOT_FOUND));
        
        PaperTemplate paperTemplate = paperTemplateMapper.toEntity(request, categoryPaper);
        PaperTemplate savedPaperTemplate = paperTemplateRepository.save(paperTemplate);
        return paperTemplateMapper.toResponse(savedPaperTemplate);
    }
    
    @Override
    @Transactional
    public PaperTemplateResponse update(Long id, PaperTemplateRequest request) {

        PaperTemplate paperTemplate = paperTemplateRepository
                .findByPaperTemplateIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAPER_TEMPLATE_NOT_FOUND));
        
        // Check if name already exists (excluding current entity)
        if (!paperTemplate.getName().equals(request.getName()) 
                && paperTemplateRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new AppException(ErrorCode.PAPER_TEMPLATE_NAME_EXISTED);
        }
        
        // Get category paper
        CategoryPaper categoryPaper = categoryPaperRepository
                .findByCategoryPaperIdAndDeletedAtIsNull(request.getCategoryPaperId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_PAPER_NOT_FOUND));
        
        paperTemplateMapper.updateEntity(paperTemplate, request, categoryPaper);
        PaperTemplate updatedPaperTemplate = paperTemplateRepository.save(paperTemplate);
        return paperTemplateMapper.toResponse(updatedPaperTemplate);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        PaperTemplate paperTemplate = paperTemplateRepository
                .findByPaperTemplateIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAPER_TEMPLATE_NOT_FOUND));
        
        paperTemplate.setDeletedAt(LocalDateTime.now());
        paperTemplateRepository.save(paperTemplate);

    }
    
    @Override
    public PaperTemplateResponse getById(Long id) {
        PaperTemplate paperTemplate = paperTemplateRepository
                .findByPaperTemplateIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAPER_TEMPLATE_NOT_FOUND));
        
        return paperTemplateMapper.toResponse(paperTemplate);
    }
    
    @Override
    public PagedResponse<PaperTemplateResponse> getAll(Long categoryId, PaperSize paperSize, String keyword, int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaperTemplate> paperTemplatePage = paperTemplateRepository.search(categoryId, paperSize, keyword, pageable);
        
        List<PaperTemplateResponse> content = paperTemplatePage.getContent().stream()
                .map(paperTemplateMapper::toResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<PaperTemplateResponse>builder()
                .content(content)
                .pageNo(paperTemplatePage.getNumber())
                .pageSize(paperTemplatePage.getSize())
                .totalElements(paperTemplatePage.getTotalElements())
                .totalPages(paperTemplatePage.getTotalPages())
                .isLast(paperTemplatePage.isLast())
                .build();
    }
}
