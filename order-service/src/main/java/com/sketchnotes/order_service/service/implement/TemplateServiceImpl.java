package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.client.ProjectClient;
import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.dtos.designer.ResourceTemplateVersionDTO;
import com.sketchnotes.order_service.dtos.project.ProjectResponse;
import com.sketchnotes.order_service.entity.ResourceTemplate;
import com.sketchnotes.order_service.entity.ResourcesTemplateImage;
import com.sketchnotes.order_service.entity.ResourceTemplateItem;
import com.sketchnotes.order_service.entity.ResourceTemplateVersion;
import com.sketchnotes.order_service.entity.ResourceTemplateVersionImage;
import com.sketchnotes.order_service.entity.ResourceTemplateVersionItem;
import com.sketchnotes.order_service.exception.ResourceTemplateNotFoundException;
import com.sketchnotes.order_service.mapper.OrderMapper;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateVersionRepository;
import com.sketchnotes.order_service.repository.UserResourceRepository;
import com.sketchnotes.order_service.service.TemplateService;
import com.sketchnotes.order_service.client.IdentityClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TemplateServiceImpl implements TemplateService {
    
    private final ResourceTemplateRepository resourceTemplateRepository;
    private final ResourceTemplateVersionRepository versionRepository;
    private final UserResourceRepository userResourceRepository;
    private final OrderMapper orderMapper;
    private final ProjectClient projectClient;
    private final com.sketchnotes.order_service.repository.OrderRepository orderRepository;
    private final IdentityClient identityClient;

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getAllActiveTemplates() {
        // State Machine: Chỉ lấy templates có status = PUBLISHED (active trên marketplace)
        return orderMapper.toTemplateDtoList(
            resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getAllActiveTemplates(int page, int size, String sortBy, String sortDir, Long currentUserId) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        // State Machine: Chỉ lấy templates có status = PUBLISHED
        Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByStatus(
            ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
        PagedResponseDTO<ResourceTemplateDTO> result = convertToPagedResponse(templatePage);
        
        // Populate statistics, designer info and owner flag
        populateStatistics(result.getContent());
        populateDesignerInfo(result.getContent());
        populateOwnerFlag(result.getContent(), currentUserId);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceTemplateDTO getTemplateById(Long id, Long currentUserId) {
        // State Machine: Chỉ lấy template có status = PUBLISHED
        ResourceTemplate template = resourceTemplateRepository.findByTemplateIdAndStatus(
            id, ResourceTemplate.TemplateStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        ResourceTemplateDTO dto = orderMapper.toDto(template);
        
        // Populate statistics, designer info and owner flag
        populateStatistics(dto);
        populateDesignerInfo(dto);
        populateOwnerFlag(dto, currentUserId);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getTemplatesByDesigner(Long designerId) {
        // State Machine: Chỉ lấy templates có status = PUBLISHED (cho customer xem)
        return orderMapper.toTemplateDtoList(
            resourceTemplateRepository.findByDesignerIdAndStatus(designerId, ResourceTemplate.TemplateStatus.PUBLISHED)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByDesigner(
            Long designerId, int page, int size, String sortBy, String sortDir, Long currentUserId) {
        // State Machine: Khi customer xem designer's products, chỉ show status = PUBLISHED
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByDesignerIdAndStatus(
            designerId, ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
        
        PagedResponseDTO<ResourceTemplateDTO> result = convertToPagedResponse(templatePage);
        populateStatistics(result.getContent());
        populateDesignerInfo(result.getContent());
        populateOwnerFlag(result.getContent(), currentUserId);
        return result;
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
        
        // NOTE: Method này được dùng bởi Designer để xem sản phẩm của mình
        // State Machine: Designer có thể xem tất cả status (PUBLISHED, ARCHIVED, PENDING_REVIEW, etc.)
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

        PagedResponseDTO<ResourceTemplateDTO> result = convertToPagedResponse(templatePage);
        populateStatistics(result.getContent());
        return result;
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
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByType(String type, int page, int size, String sortBy, String sortDir, Long currentUserId) {
        try {
            ResourceTemplate.TemplateType templateType = ResourceTemplate.TemplateType.valueOf(type.toUpperCase());
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            // State Machine: Chỉ lấy templates có status = PUBLISHED
            Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByTypeAndStatus(
                templateType, ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
            PagedResponseDTO<ResourceTemplateDTO> result = convertToPagedResponse(templatePage);
            populateStatistics(result.getContent());
            populateDesignerInfo(result.getContent());
            populateOwnerFlag(result.getContent(), currentUserId);
            return result;
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
    public PagedResponseDTO<ResourceTemplateDTO> searchTemplates(String keyword, int page, int size, String sortBy, String sortDir, Long currentUserId) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        // State Machine: Chỉ tìm templates có status = PUBLISHED
        Page<ResourceTemplate> templatePage = resourceTemplateRepository.searchByKeyword(
            keyword, ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
        PagedResponseDTO<ResourceTemplateDTO> result = convertToPagedResponse(templatePage);
        populateStatistics(result.getContent());
        populateDesignerInfo(result.getContent());
        populateOwnerFlag(result.getContent(), currentUserId);
        return result;
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
        PagedResponseDTO<ResourceTemplateDTO> result = convertToPagedResponse(templatePage);
        populateStatistics(result.getContent());
        return result;
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
                        it.setImageUrl(itemDto.getImageUrl());
                        it.setResourceTemplate(template);
                        return it;
                    }).toList();
            // FIX: Don't replace collection reference - clear and addAll instead
            // to avoid "orphan deletion was no longer referenced" error
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
        
        // Save template first to get ID
        ResourceTemplate saved = resourceTemplateRepository.save(template);
        
        // 🔹 AUTO-CREATE VERSION 1.0 with PENDING_REVIEW status
        ResourceTemplateVersion version = new ResourceTemplateVersion();
        version.setTemplateId(saved.getTemplateId());
        version.setVersionNumber("1.0");
        version.setName(saved.getName());
        version.setDescription(saved.getDescription());
        version.setPrice(saved.getPrice());
        version.setType(saved.getType());
        version.setStatus(ResourceTemplate.TemplateStatus.PENDING_REVIEW);
        version.setCreatedBy(templateDTO.getDesignerId());
        
        // Copy images to version
        if (saved.getImages() != null && !saved.getImages().isEmpty()) {
            List<ResourceTemplateVersionImage> versionImages = saved.getImages().stream()
                    .map(img -> {
                        ResourceTemplateVersionImage vImg = new ResourceTemplateVersionImage();
                        vImg.setImageUrl(img.getImageUrl());
                        vImg.setIsThumbnail(img.getIsThumbnail());
                        vImg.setVersion(version);
                        return vImg;
                    }).toList();
            version.getImages().clear();
            version.getImages().addAll(versionImages);
        }
        
        // Copy items to version
        if (saved.getItems() != null && !saved.getItems().isEmpty()) {
            List<ResourceTemplateVersionItem> versionItems = saved.getItems().stream()
                    .map(item -> {
                        ResourceTemplateVersionItem vItem = new ResourceTemplateVersionItem();
                        vItem.setItemIndex(item.getItemIndex());
                        vItem.setItemUrl(item.getItemUrl());
                        vItem.setImageUrl(item.getImageUrl());
                        vItem.setVersion(version);
                        return vItem;
                    }).toList();
            version.getItems().clear();
            version.getItems().addAll(versionItems);
        }
        
        versionRepository.save(version);
        
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
        // This method is deprecated as expiredTime field has been removed
        return java.util.Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getLatestTemplates(int limit, Long currentUserId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ResourceTemplate> templates = resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED).stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(limit)
                .toList();
        List<ResourceTemplateDTO> result = orderMapper.toTemplateDtoList(templates);
        populateStatistics(result);
        populateDesignerInfo(result);
        populateOwnerFlag(result, currentUserId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceTemplateDTO> getPopularTemplates(int limit, Long currentUserId) {
        // For now, return templates sorted by price (as a simple popularity metric)
        // In a real implementation, this would be based on order count or views
        List<ResourceTemplate> templates = resourceTemplateRepository.findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED).stream()
                .sorted((t1, t2) -> t2.getPrice().compareTo(t1.getPrice()))
                .limit(limit)
                .toList();
        List<ResourceTemplateDTO> result = orderMapper.toTemplateDtoList(templates);
        populateStatistics(result);
        populateDesignerInfo(result);
        populateOwnerFlag(result, currentUserId);
        return result;
    }

    // Helper method to convert Page to PagedResponseDTO
    @Override
    public ResourceTemplateDTO confirmTemplate(Long id) {
        // 1. Tìm template
        ResourceTemplate template = resourceTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + id));
        
        // 2. Tìm tất cả version PENDING_REVIEW của template này, chọn version mới nhất
        List<ResourceTemplateVersion> pendingVersions = versionRepository.findByTemplateIdAndStatusOrderByCreatedAtDesc(
            id, ResourceTemplate.TemplateStatus.PENDING_REVIEW);
        if (pendingVersions.isEmpty()) {
            throw new IllegalStateException("No pending version found for template " + id);
        }
//        if (pendingVersions.size() > 1) {
//            log.warn("Template {} has {} PENDING_REVIEW versions. Only the newest will be approved.", id, pendingVersions.size());
//        }
        ResourceTemplateVersion pendingVersion = pendingVersions.get(0); // newest
        
        // 3. Approve version: PENDING_REVIEW -> PUBLISHED
        pendingVersion.setStatus(ResourceTemplate.TemplateStatus.PUBLISHED);
        pendingVersion.setReviewedAt(java.time.LocalDateTime.now());
        versionRepository.save(pendingVersion);
        
        // 4. Sync metadata từ version lên template
        template.setName(pendingVersion.getName());
        template.setDescription(pendingVersion.getDescription());
        template.setPrice(pendingVersion.getPrice());
        template.setType(pendingVersion.getType());
        
        // 5. Auto-publish version này làm version chính thức
        template.setStatus(ResourceTemplate.TemplateStatus.PUBLISHED);
        template.setCurrentPublishedVersionId(pendingVersion.getVersionId());
        
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
        
        // 🔹 Find the PENDING_REVIEW version for this template
        ResourceTemplateVersion pendingVersion = versionRepository
                .findByTemplateIdAndStatus(id, ResourceTemplate.TemplateStatus.PENDING_REVIEW)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pending version found for template " + id));
        
        // 🔹 Update version status to REJECTED
        pendingVersion.setStatus(ResourceTemplate.TemplateStatus.REJECTED);
        pendingVersion.setReviewedAt(java.time.LocalDateTime.now());
        // Optional: Set review comment if needed
        // pendingVersion.setReviewComment("Rejected by admin");
        versionRepository.save(pendingVersion);
        
        // 🔹 Update template status
        template.setStatus(ResourceTemplate.TemplateStatus.REJECTED);
        ResourceTemplate saved = resourceTemplateRepository.save(template);
        return orderMapper.toDto(saved);
    }

    @Override
    public PagedResponseDTO<ResourceTemplateVersionDTO> getPendingVersions(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResourceTemplateVersion> pending = versionRepository.findByStatusOrderByCreatedAtDesc(
                ResourceTemplate.TemplateStatus.PENDING_REVIEW, pageable);

        List<ResourceTemplateVersionDTO> content = pending.getContent().stream()
                .map(orderMapper::toVersionDto)
                .toList();

        return PagedResponseDTO.<ResourceTemplateVersionDTO>builder()
                .content(content)
                .page(pending.getNumber())
                .size(pending.getSize())
                .totalElements(pending.getTotalElements())
                .totalPages(pending.getTotalPages())
                .first(pending.isFirst())
                .last(pending.isLast())
                .hasNext(pending.hasNext())
                .hasPrevious(pending.hasPrevious())
                .build();
    }

    @Override
    public ResourceTemplateVersionDTO reviewVersion(Long versionId, Long staffId, boolean approve, String reviewComment) {
        ResourceTemplateVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Version not found with id: " + versionId));

        if (!ResourceTemplate.TemplateStatus.PENDING_REVIEW.equals(version.getStatus())) {
            throw new IllegalStateException("Version is not in PENDING_REVIEW status");
        }

        ResourceTemplate template = resourceTemplateRepository.findById(version.getTemplateId())
                .orElseThrow(() -> new ResourceTemplateNotFoundException("Template not found with id: " + version.getTemplateId()));

        version.setReviewedAt(java.time.LocalDateTime.now());
        version.setReviewedBy(staffId);

        // Check if this is a NEW version (not the first version of the template)
        boolean isNewVersionForExistingTemplate = template.getCurrentPublishedVersionId() != null 
                && !template.getCurrentPublishedVersionId().equals(versionId);

        if (approve) {
            version.setStatus(ResourceTemplate.TemplateStatus.PUBLISHED);

            // Sync metadata from version to template and mark as published/current
            template.setName(version.getName());
            template.setDescription(version.getDescription());
            template.setType(version.getType());
            template.setPrice(version.getPrice());
            template.setReleaseDate(version.getReleaseDate());

            template.setStatus(ResourceTemplate.TemplateStatus.PUBLISHED);
            template.setCurrentPublishedVersionId(version.getVersionId());
            
            versionRepository.save(version);
            resourceTemplateRepository.save(template);
            
            // Send notification to all customers who own this resource about the new version
            if (isNewVersionForExistingTemplate) {
                notifyCustomersAboutNewVersion(template, version);
            }
        } else {
            version.setStatus(ResourceTemplate.TemplateStatus.REJECTED);
            version.setReviewComment(reviewComment);

            // Only set template to REJECTED if it has no published version yet
            if (template.getCurrentPublishedVersionId() == null
                    && ResourceTemplate.TemplateStatus.PENDING_REVIEW.equals(template.getStatus())) {
                template.setStatus(ResourceTemplate.TemplateStatus.REJECTED);
            }
            
            versionRepository.save(version);
            resourceTemplateRepository.save(template);
        }

        return orderMapper.toVersionDto(version);
    }
    
    /**
     * Notify all customers who own this resource about a new version being available.
     * Customers will receive a notification and can choose to upgrade for free.
     */
    private void notifyCustomersAboutNewVersion(ResourceTemplate template, ResourceTemplateVersion newVersion) {
        try {
            // Find all users who own this resource and are not already on the latest version
            List<com.sketchnotes.order_service.entity.UserResource> owners = 
                    userResourceRepository.findByResourceTemplateIdAndActiveTrue(template.getTemplateId());
            
            String resourceName = template.getName();
            String newVersionNumber = newVersion.getVersionNumber();
            
            for (com.sketchnotes.order_service.entity.UserResource owner : owners) {
                // Only notify users who are not already on the latest version
                Long userCurrentVersionId = owner.getCurrentVersionId();
                if (userCurrentVersionId == null || !userCurrentVersionId.equals(newVersion.getVersionId())) {
                    try {
                        CreateNotificationRequest notification = CreateNotificationRequest.builder()
                                .userId(owner.getUserId())
                                .title("New Version Available")
                                .message(String.format(
                                        "A new version (%s) of the resource \"%s\" is available. You can upgrade for free.",
                                        newVersionNumber, resourceName))
                                .type("VERSION_AVAILABLE")
                                .resourceItemId(template.getTemplateId())
                                .build();
                        
                        identityClient.createNotification(notification);
                        log.info("Sent new version notification to user {} for resource {}", 
                                owner.getUserId(), template.getTemplateId());
                    } catch (Exception e) {
                        log.error("Failed to send notification to user {} for resource {}: {}", 
                                owner.getUserId(), template.getTemplateId(), e.getMessage());
                        // Continue with other users even if one notification fails
                    }
                }
            }
            
            log.info("Completed sending new version notifications for resource {} version {}", 
                    template.getTemplateId(), newVersion.getVersionNumber());
        } catch (Exception e) {
            log.error("Error while sending new version notifications for resource {}: {}", 
                    template.getTemplateId(), e.getMessage());
            // Don't fail the version approval if notifications fail
        }
    }

    @Override
    public ResourceTemplateDTO createTemplateFromProject(Long projectId, Long userId, TemplateSellDTO templateDTO) {
        // 🔹 1. Lấy project từ ProjectClient
        ApiResponse<ProjectResponse> projectApiResponse = projectClient.getProject(projectId);
        if (projectApiResponse == null || projectApiResponse.getResult() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        ProjectResponse project = projectApiResponse.getResult();

        // 🔹 2. Map dữ liệu từ Project + DTO
        ResourceTemplate template = new ResourceTemplate();
        template.setName(templateDTO.getName() != null && !templateDTO.getName().isEmpty() 
                ? templateDTO.getName() 
                : project.getName());
        template.setDescription(templateDTO.getDescription() != null && !templateDTO.getDescription().isEmpty() 
                ? templateDTO.getDescription() 
                : project.getDescription());
        
        // Convert type string to enum
        if (templateDTO.getType() != null) {
            try {
                template.setType(ResourceTemplate.TemplateType.valueOf(templateDTO.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // List all valid template types
                String validTypes = String.join(", ", 
                    java.util.Arrays.stream(ResourceTemplate.TemplateType.values())
                        .map(Enum::name)
                        .toArray(String[]::new));
                throw new IllegalArgumentException(
                    "Invalid template type: '" + templateDTO.getType() + "'. " +
                    "Valid types are: " + validTypes);
            }
        } else {
            throw new IllegalArgumentException("Template type is required");
        }
        
        template.setPrice(templateDTO.getPrice());
        
        template.setReleaseDate(LocalDate.now());
        template.setDesignerId(userId);
        template.setStatus(ResourceTemplate.TemplateStatus.PENDING_REVIEW);

        // 🔹 3. Gán hình ảnh thumbnail
        if (project.getImageUrl() != null && !project.getImageUrl().isEmpty()) {
            ResourcesTemplateImage thumbnail = new ResourcesTemplateImage();
            thumbnail.setImageUrl(project.getImageUrl());
            thumbnail.setIsThumbnail(true);
            thumbnail.setResourceTemplate(template);
            template.getImages().add(thumbnail);
        }

        // 🔹 4. Gán các page → item
        if (project.getPages() != null && !project.getPages().isEmpty()) {
            project.getPages().forEach(p -> {
                ResourceTemplateItem item = new ResourceTemplateItem();
                item.setItemIndex(p.getPageNumber());
                item.setItemUrl(projectClient.copyFile(FileRequest.builder()
                        .sourceFileUrl(p.getStrokeUrl())
                        .build()).getResult().get("newFileUrl")); // Copy strokeUrl sang storage của order_service
                item.setImageUrl(p.getSnapshotUrl()); // Set image_url từ snapshotUrl của page
                item.setResourceTemplate(template);
                template.getItems().add(item);
            });
        }

        // 🔹 5. Lưu vào DB
        ResourceTemplate saved = resourceTemplateRepository.save(template);
        // 🔹 6. AUTO-CREATE VERSION 1.0 with PENDING_REVIEW status
        ResourceTemplateVersion version = new ResourceTemplateVersion();
        version.setTemplateId(saved.getTemplateId());
        version.setVersionNumber("1.0");
        version.setName(saved.getName());
        version.setDescription(saved.getDescription());
        version.setTemplateId(saved.getTemplateId());
        version.setPrice(saved.getPrice());
        version.setType(saved.getType());
        version.setReleaseDate(saved.getReleaseDate());
        version.setStatus(ResourceTemplate.TemplateStatus.PENDING_REVIEW);
        version.setCreatedBy(userId);
        versionRepository.save(version);
        // Copy images to version
        if (saved.getImages() != null && !saved.getImages().isEmpty()) {
            List<ResourceTemplateVersionImage> versionImages = saved.getImages().stream()
                    .map(img -> {
                        ResourceTemplateVersionImage vImg = new ResourceTemplateVersionImage();
                        vImg.setImageUrl(img.getImageUrl());
                        vImg.setIsThumbnail(img.getIsThumbnail());
                        vImg.setVersion(version);
                        return vImg;
                    }).toList();
            version.getImages().clear();
            version.getImages().addAll(versionImages);
        }

        // Copy items to version
        if (saved.getItems() != null && !saved.getItems().isEmpty()) {
            List<ResourceTemplateVersionItem> versionItems = saved.getItems().stream()
                    .map(item -> {
                        ResourceTemplateVersionItem vItem = new ResourceTemplateVersionItem();
                        vItem.setItemIndex(item.getItemIndex());
                        vItem.setItemUrl(item.getItemUrl());
                        vItem.setImageUrl(item.getImageUrl());
                        vItem.setVersion(version);
                        return vItem;
                    }).toList();
            version.getItems().clear();
            version.getItems().addAll(versionItems);
        }

        return orderMapper.toDto(saved);
    }

    @Override
    public PagedResponseDTO<ResourceTemplateDTO> getTemplatesByReviewStatus(String status, int page, int size, String sortBy, String sortDir) {
        try {
            ResourceTemplate.TemplateStatus templateStatus = ResourceTemplate.TemplateStatus.valueOf(status.toUpperCase());
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ResourceTemplate> templatePage = resourceTemplateRepository.findByStatus(templateStatus, pageable);
            PagedResponseDTO<ResourceTemplateDTO> result = convertToPagedResponse(templatePage);
            populateStatistics(result.getContent());
            return result;
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
    
    /**
     * Populate statistics fields for a single template DTO
     */
    private void populateStatistics(ResourceTemplateDTO dto) {
        if (dto == null || dto.getResourceTemplateId() == null) {
            return;
        }
        
        try {
            // Get purchase count from OrderRepository
            Long purchaseCount = orderRepository.countSuccessfulOrdersByTemplateId(dto.getResourceTemplateId());
            dto.setPurchaseCount(purchaseCount != null ? purchaseCount : 0L);
            
            // Get feedback statistics from IdentityClient
            try {
                var feedbackResponse = identityClient.getFeedbackStats(dto.getResourceTemplateId());
                if (feedbackResponse != null && feedbackResponse.getResult() != null) {
                    FeedbackStatsResponse stats = feedbackResponse.getResult();
                    dto.setFeedbackCount(stats.getTotalFeedbacks() != null ? stats.getTotalFeedbacks() : 0L);
                    dto.setAverageRating(stats.getAverageRating());
                    dto.setAvgResourceRating(stats.getAverageRating());
                } else {
                    dto.setFeedbackCount(0L);
                    dto.setAverageRating(null);
                    dto.setAvgResourceRating(null);
                }
            } catch (Exception e) {
                // If feedback service is unavailable, set default values
                dto.setFeedbackCount(0L);
                dto.setAverageRating(null);
                dto.setAvgResourceRating(null);
            }
        } catch (Exception e) {
            // If any error occurs, set default values
            dto.setPurchaseCount(0L);
            dto.setFeedbackCount(0L);
            dto.setAverageRating(null);
            dto.setAvgResourceRating(null);
        }
    }
    
    /**
     * Populate statistics fields for a list of template DTOs
     */
    private void populateStatistics(List<ResourceTemplateDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        
        dtos.forEach(this::populateStatistics);
    }

    /**
     * Populate designer info for a single template DTO
     */
    private void populateDesignerInfo(ResourceTemplateDTO dto) {
        if (dto == null || dto.getDesignerId() == null) {
            return;
        }

        try {
            var apiResponse = identityClient.getUser(dto.getDesignerId());
            if (apiResponse != null && apiResponse.getResult() != null) {
                UserResponse user = apiResponse.getResult();
                DesignerInfoDTO designerInfo = new DesignerInfoDTO();
                designerInfo.setEmail(user.getEmail());
                designerInfo.setFirstName(user.getFirstName());
                designerInfo.setLastName(user.getLastName());
                designerInfo.setAvatarUrl(user.getAvatarUrl());
                dto.setDesignerInfo(designerInfo);
            }
        } catch (Exception e) {
            log.warn("Failed to get designer info for designerId {}: {}", dto.getDesignerId(), e.getMessage());
        }
    }

    /**
     * Populate designer info for a list of template DTOs
     */
    private void populateDesignerInfo(List<ResourceTemplateDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        dtos.forEach(this::populateDesignerInfo);
    }

    /**
     * Populate isOwner flag for a single template DTO
     * isOwner = true if user is the designer OR user has purchased the template
     */
    private void populateOwnerFlag(ResourceTemplateDTO dto, Long currentUserId) {
        if (dto == null || currentUserId == null) {
            return;
        }

        boolean isDesigner = currentUserId.equals(dto.getDesignerId());
        boolean hasPurchased = userResourceRepository.existsByUserIdAndResourceTemplateIdAndActiveTrue(
                currentUserId, dto.getResourceTemplateId());
        dto.setIsOwner(isDesigner || hasPurchased);
    }

    /**
     * Populate isOwner flag for a list of template DTOs
     * isOwner = true if user is the designer OR user has purchased the template
     */
    private void populateOwnerFlag(List<ResourceTemplateDTO> dtos, Long currentUserId) {
        if (dtos == null || dtos.isEmpty() || currentUserId == null) {
            return;
        }

        // Get all template IDs that user has purchased (batch query for performance)
        List<Long> purchasedTemplateIds = userResourceRepository.findActiveTemplateIdsByUserId(currentUserId);
        Set<Long> purchasedSet = purchasedTemplateIds != null ?
                new HashSet<>(purchasedTemplateIds) : Set.of();

        dtos.forEach(dto -> {
            boolean isDesigner = currentUserId.equals(dto.getDesignerId());
            boolean hasPurchased = purchasedSet.contains(dto.getResourceTemplateId());
            dto.setIsOwner(isDesigner || hasPurchased);
        });
    }
}
