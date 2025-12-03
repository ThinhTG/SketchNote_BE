package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.dtos.mapper.CategoryPaperMapper;
import com.sketchnotes.project_service.dtos.request.CategoryPaperRequest;
import com.sketchnotes.project_service.dtos.response.CategoryPaperResponse;
import com.sketchnotes.project_service.entity.CategoryPaper;
import com.sketchnotes.project_service.enums.PaperType;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.repository.ICategoryPaperRepository;
import com.sketchnotes.project_service.service.ICategoryPaperService;
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
public class CategoryPaperService implements ICategoryPaperService {
    
    private final ICategoryPaperRepository categoryPaperRepository;
    private final CategoryPaperMapper categoryPaperMapper;
    
    @Override
    @Transactional
    public CategoryPaperResponse create(CategoryPaperRequest request) {
        // Check if name already exists
        if (categoryPaperRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_PAPER_NAME_EXISTED);
        }
        
        CategoryPaper categoryPaper = categoryPaperMapper.toEntity(request);
        CategoryPaper savedCategoryPaper = categoryPaperRepository.save(categoryPaper);
        
        return categoryPaperMapper.toResponse(savedCategoryPaper);
    }
    
    @Override
    @Transactional
    public CategoryPaperResponse update(Long id, CategoryPaperRequest request) {

        CategoryPaper categoryPaper = categoryPaperRepository
                .findByCategoryPaperIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_PAPER_NOT_FOUND));
        
        // Check if name already exists (excluding current entity)
        if (!categoryPaper.getName().equals(request.getName()) 
                && categoryPaperRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_PAPER_NAME_EXISTED);
        }
        
        categoryPaperMapper.updateEntity(categoryPaper, request);
        CategoryPaper updatedCategoryPaper = categoryPaperRepository.save(categoryPaper);
        
        return categoryPaperMapper.toResponse(updatedCategoryPaper);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        CategoryPaper categoryPaper = categoryPaperRepository
                .findByCategoryPaperIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_PAPER_NOT_FOUND));
        
        categoryPaper.setDeletedAt(LocalDateTime.now());
        categoryPaperRepository.save(categoryPaper);
        
    }
    
    @Override
    public CategoryPaperResponse getById(Long id) {

        CategoryPaper categoryPaper = categoryPaperRepository
                .findByCategoryPaperIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_PAPER_NOT_FOUND));
        
        return categoryPaperMapper.toResponse(categoryPaper);
    }
    
    @Override
    public PagedResponse<CategoryPaperResponse> getAll(PaperType paperType, String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CategoryPaper> categoryPaperPage = categoryPaperRepository.search(paperType, keyword, pageable);
        
        List<CategoryPaperResponse> content = categoryPaperPage.getContent().stream()
                .map(categoryPaperMapper::toResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<CategoryPaperResponse>builder()
                .content(content)
                .pageNo(categoryPaperPage.getNumber())
                .pageSize(categoryPaperPage.getSize())
                .totalElements(categoryPaperPage.getTotalElements())
                .totalPages(categoryPaperPage.getTotalPages())
                .isLast(categoryPaperPage.isLast())
                .build();
    }
}
