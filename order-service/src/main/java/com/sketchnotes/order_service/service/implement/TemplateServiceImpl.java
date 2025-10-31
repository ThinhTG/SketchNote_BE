package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.dtos.PagedResponseDTO;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.dtos.TemplateCreateUpdateDTO;
import com.sketchnotes.order_service.entity.ResourceTemplate;
import com.sketchnotes.order_service.entity.ResourcesTemplateImage;
import com.sketchnotes.order_service.entity.ResourceTemplateItem;
import com.sketchnotes.order_service.exception.ResourceTemplateNotFoundException;
import com.sketchnotes.order_service.mapper.OrderMapper;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateServiceImpl implements TemplateService {
    
    private final ResourceTemplateRepository resourceTemplateRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getAllActiveTemplates() {
        return orderMapper.toTemplateDtoList(
            resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getAllActiveTemplates(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByStatus(
            ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
        return convertToPagedResponse(templatePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceTemplateDTO getTemplateById(Long id) {
    ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndStatus(id, ResourceTemplate.TemplateStatus.PUBLISHED)
        .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        return orderMapper.toDto(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByDesigner(Long designerId) {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByDesignerIdAndStatus(designerId, ResourceTemplate.TemplateStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByDesigner(
            Long designerId, int page, int size, String sortBy, String sortDir) {
        return getTemplatesByDesignerAndStatus(designerId, null, page, size, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByDesignerAndStatus(
            Long designerId, String status, int page, int size, String sortBy, String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ResourceTemplate> templatePage;
        
        if (status != null) {
            try {
                ResourceTemplate.TemplateStatus templateStatus = ResourceTemplate.TemplateStatus.valueOf(status.toUpperCase());
                templatePage = resourceTemplateRepository.findByDesignerIdAndStatus(designerId, templateStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid template status: " + status);
            }
        } else {
            // If no status is provided, get all templates regardless of status
            templatePage = resourceTemplateRepository.findByDesignerId(designerId, pageable);
        }

        return convertToPagedResponse(templatePage);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByType(String type) {
        try {
            ResourceTemplate.TemplateType templateType = ResourceTemplate.TemplateType.valueOf(type.toUpperCase());
            return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByTypeAndStatus(templateType, ResourceTemplate.TemplateStatus.PUBLISHED));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid template type: " + type);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByType(String type, int page, int size, String sortBy, String sortDir) {
        try {
            ResourceTemplate.TemplateType templateType = ResourceTemplate.TemplateType.valueOf(type.toUpperCase());
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByTypeAndStatus(templateType, ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
            return convertToPagedResponse(templatePage);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid template type: " + type);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> searchTemplates(String keyword) {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.searchByKeyword(keyword, ResourceTemplate.TemplateStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> searchTemplates(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
    Page<ResourceTemplate> templatePage = resourceTemplateRepository.searchByKeyword(keyword, ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
        return convertToPagedResponse(templatePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByPriceRange(minPrice, maxPrice, ResourceTemplate.TemplateStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
    Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByPriceRange(minPrice, maxPrice, ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
        return convertToPagedResponse(templatePage);
    }

    @Override
    public ResourceTemplateDTO createTemplate(TemplateCreateUpdateDTO templateDTO) {
        ResourceTemplate template = orderMapper.toEntity(templateDTO);
        template.setStatus(ResourceTemplate.TemplateStatus.PENDING_REVIEW);

        // Attach images from DTO (if any) - ensure bidirectional relation
        if (templateDTO.getImages() != null && !templateDTO.getImages().isEmpty()) {
            java.util.List<ResourcesTemplateImage> imageEntities = templateDTO.getImages().stream()
                    .map(imgDto -> {
                        ResourcesTemplateImage img = new ResourcesTemplateImage();
                        img.setImageUrl(imgDto.getImageUrl());
                        img.setIsThumbnail(imgDto.getIsThumbnail());
                        img.setResourceTemplate(template);
                        return img;
                    }).toList();
            template.getImages().clear();
            template.getImages().addAll(imageEntities);
        }

        // Attach items from DTO (if any)
        if (templateDTO.getItems() != null && !templateDTO.getItems().isEmpty()) {
            List<ResourceTemplateItem> itemEntities = templateDTO.getItems().stream()
                    .map(itemDto -> {
                        ResourceTemplateItem it = new ResourceTemplateItem();
                        it.setItemIndex(itemDto.getItemIndex());
                        it.setItemUrl(itemDto.getItemUrl());
                        it.setResourceTemplate(template);
                        return it;
                    }).toList();
            template.setItems(itemEntities);
        }


        // Set type if provided
        if (templateDTO.getType() != null) {
            try {
                template.setType(ResourceTemplate.TemplateType.valueOf(templateDTO.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid template type: " + templateDTO.getType());
            }
        }
        
        ResourceTemplate saved = resourceTemplateRepository.save(template);
        return orderMapper.toDto(saved);
    }

    @Override
    public ResourceTemplateDTO updateTemplate(Long id, TemplateCreateUpdateDTO templateDTO) {
        ResourceTemplate existingTemplate = resourceTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        
        // Update fields
        existingTemplate.setName(templateDTO.getName());
        existingTemplate.setDescription(templateDTO.getDescription());
        existingTemplate.setPrice(templateDTO.getPrice());
        existingTemplate.setExpiredTime(templateDTO.getExpiredTime());
        existingTemplate.setReleaseDate(templateDTO.getReleaseDate());
        
        if (templateDTO.getType() != null) {
            try {
                existingTemplate.setType(ResourceTemplate.TemplateType.valueOf(templateDTO.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid template type: " + templateDTO.getType());
            }
        }
        
        ResourceTemplate saved = resourceTemplateRepository.save(existingTemplate);
        return orderMapper.toDto(saved);
    }

    @Override
    public void deleteTemplate(Long id) {
        ResourceTemplate template = resourceTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        // Instead of setting isActive to false, we'll mark it as REJECTED
        template.setStatus(ResourceTemplate.TemplateStatus.REJECTED);
        resourceTemplateRepository.save(template);
    }

    @Override
    public ResourceTemplateDTO toggleTemplateStatus(Long id) {
        ResourceTemplate template = resourceTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        // Toggle between PUBLISHED and REJECTED
        template.setStatus(template.getStatus() == ResourceTemplate.TemplateStatus.PUBLISHED ? 
            ResourceTemplate.TemplateStatus.REJECTED : ResourceTemplate.TemplateStatus.PUBLISHED);
        ResourceTemplate saved = resourceTemplateRepository.save(template);
        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByStatus(Boolean isActive) {
        List<ResourceTemplate> templates = isActive ? 
                resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED) :
                resourceTemplateRepository.findAll().stream()
                        .filter(t -> !t.getStatus().equals(ResourceTemplate.TemplateStatus.PUBLISHED))
                        .toList();
        return orderMapper.toTemplateDtoList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesExpiringSoon(int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        List<ResourceTemplate> templates = resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED).stream()
                .filter(t -> t.getExpiredTime() != null && t.getExpiredTime().isBefore(expiryDate))
                .toList();
        return orderMapper.toTemplateDtoList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getLatestTemplates(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ResourceTemplate> templates = resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED).stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(limit)
                .toList();
        return orderMapper.toTemplateDtoList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getPopularTemplates(int limit) {
        // For now, return templates sorted by price (as a simple popularity metric)
        // In a real implementation, this would be based on order count or views
        List<ResourceTemplate> templates = resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED).stream()
                .sorted((t1, t2) -> t2.getPrice().compareTo(t1.getPrice()))
                .limit(limit)
                .toList();
        return orderMapper.toTemplateDtoList(templates);
    }

    // Helper method to convert Page to PagedResponseDTO
    @Override
    public ResourceTemplateDTO confirmTemplate(Long id) {
        ResourceTemplate template = resourceTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        
        // Validate current status
        if (template.getStatus() != ResourceTemplate.TemplateStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Template with id " + id + " is not in PENDING_REVIEW status");
        }
        
        template.setStatus(ResourceTemplate.TemplateStatus.PUBLISHED);
        ResourceTemplate saved = resourceTemplateRepository.save(template);
        return orderMapper.toDto(saved);
    }

    @Override
    public ResourceTemplateDTO rejectTemplate(Long id) {
        ResourceTemplate template = resourceTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        
        // Validate current status
        if (template.getStatus() != ResourceTemplate.TemplateStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Template with id " + id + " is not in PENDING_REVIEW status");
        }
        
        template.setStatus(ResourceTemplate.TemplateStatus.REJECTED);
        ResourceTemplate saved = resourceTemplateRepository.save(template);
        return orderMapper.toDto(saved);
    }

    @Override
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByReviewStatus(String status, int page, int size, String sortBy, String sortDir) {
        try {
            ResourceTemplate.TemplateStatus templateStatus = ResourceTemplate.TemplateStatus.valueOf(status.toUpperCase());
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByStatus(templateStatus, pageable);
            return convertToPagedResponse(templatePage);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid template status: " + status);
        }
    }

    private PagedResponseDTO<ResourceTemplateDTO> convertToPagedResponse(Page<ResourceTemplate> page) {
        List<ResourceTemplateDTO> content = orderMapper.toTemplateDtoList(page.getContent());
        
        return PagedResponseDTO.<ResourceTemplateDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
