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
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getAllActiveTemplates(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByIsActiveTrue(pageable);
        return convertToPagedResponse(templatePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceTemplateDTO getTemplateById(Long id) {
        ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        return orderMapper.toDto(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByDesigner(Long designerId) {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByDesignerIdAndIsActiveTrue(designerId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByDesigner(Long designerId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByDesignerIdAndIsActiveTrue(designerId, pageable);
        return convertToPagedResponse(templatePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByType(String type) {
        try {
            ResourceTemplate.TemplateType templateType = ResourceTemplate.TemplateType.valueOf(type.toUpperCase());
            return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByTypeAndIsActiveTrue(templateType));
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
            Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByTypeAndIsActiveTrue(templateType, pageable);
            return convertToPagedResponse(templatePage);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid template type: " + type);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> searchTemplates(String keyword) {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.searchByKeyword(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> searchTemplates(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResourceTemplate> templatePage = resourceTemplateRepository.searchByKeyword(keyword, pageable);
        return convertToPagedResponse(templatePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return orderMapper.toTemplateDtoList(resourceTemplateRepository.findByPriceRange(minPrice, maxPrice));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return convertToPagedResponse(templatePage);
    }

    @Override
    public ResourceTemplateDTO createTemplate(TemplateCreateUpdateDTO templateDTO) {
        ResourceTemplate template = orderMapper.toEntity(templateDTO);
        template.setIsActive(true);

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
            java.util.List<ResourceTemplateItem> itemEntities = templateDTO.getItems().stream()
                    .map(itemDto -> {
                        ResourceTemplateItem it = new ResourceTemplateItem();
                        it.setItemIndex(itemDto.getItemIndex());
                        it.setItemUrl(itemDto.getItemUrl());
                        it.setResourceTemplate(template);
                        return it;
                    }).toList();
            template.getItems().clear();
            template.getItems().addAll(itemEntities);
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
        template.setIsActive(false);
        resourceTemplateRepository.save(template);
    }

    @Override
    public ResourceTemplateDTO toggleTemplateStatus(Long id) {
        ResourceTemplate template = resourceTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        template.setIsActive(!template.getIsActive());
        ResourceTemplate saved = resourceTemplateRepository.save(template);
        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByStatus(Boolean isActive) {
        List<ResourceTemplate> templates = isActive ? 
                resourceTemplateRepository.findByIsActiveTrue() : 
                resourceTemplateRepository.findAll().stream()
                        .filter(t -> !t.getIsActive())
                        .toList();
        return orderMapper.toTemplateDtoList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesExpiringSoon(int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        List<ResourceTemplate> templates = resourceTemplateRepository.findByIsActiveTrue().stream()
                .filter(t -> t.getExpiredTime() != null && t.getExpiredTime().isBefore(expiryDate))
                .toList();
        return orderMapper.toTemplateDtoList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getLatestTemplates(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ResourceTemplate> templates = resourceTemplateRepository.findByIsActiveTrue().stream()
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
        List<ResourceTemplate> templates = resourceTemplateRepository.findByIsActiveTrue().stream()
                .sorted((t1, t2) -> t2.getPrice().compareTo(t1.getPrice()))
                .limit(limit)
                .toList();
        return orderMapper.toTemplateDtoList(templates);
    }

    // Helper method to convert Page to PagedResponseDTO
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
