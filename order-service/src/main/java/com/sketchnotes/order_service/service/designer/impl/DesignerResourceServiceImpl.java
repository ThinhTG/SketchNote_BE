package com.sketchnotes.order_service.service.designer.impl;

import com.sketchnotes.order_service.client.ProjectClient;
import com.sketchnotes.order_service.dtos.PagedResponseDTO;
import com.sketchnotes.order_service.dtos.ResourceImageDTO;
import com.sketchnotes.order_service.dtos.designer.CreateResourceVersionDTO;
import com.sketchnotes.order_service.dtos.designer.DesignerProductDTO;
import com.sketchnotes.order_service.dtos.designer.ResourceTemplateVersionDTO;
import com.sketchnotes.order_service.entity.*;
import com.sketchnotes.order_service.mapper.OrderMapper;
import com.sketchnotes.order_service.repository.DashboardRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateVersionRepository;
import com.sketchnotes.order_service.service.designer.DesignerResourceService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DesignerResourceServiceImpl implements DesignerResourceService {
    private final ResourceTemplateRepository resourceTemplateRepository;
    private final ResourceTemplateVersionRepository versionRepository;
    private final OrderMapper orderMapper;
    private final DashboardRepository dashboardRepository;
    private final ProjectClient projectClient;

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<DesignerProductDTO> getMyProducts(Long designerId, int page, int size, String sortBy, String sortDir, String search, ResourceTemplate.TemplateStatus statusFilter) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ResourceTemplate> templates;
        
        // Determine which query to use based on filters
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasStatusFilter = statusFilter != null;
        
        if (hasSearch && hasStatusFilter) {
            // Both search and status filter (e.g., ARCHIVED)
            templates = resourceTemplateRepository.searchByDesignerIdAndKeywordAndStatus(
                designerId, search.trim(), statusFilter, pageable);
        } else if (hasSearch) {
            // Only search filter
            templates = resourceTemplateRepository.searchByDesignerIdAndKeyword(
                designerId, search.trim(), pageable);
        } else if (hasStatusFilter) {
            // Only status filter (e.g., ARCHIVED, PUBLISHED, etc.)
            templates = resourceTemplateRepository.findByDesignerIdAndStatus(
                designerId, statusFilter, pageable);
        } else {
            // No filters, get all
            templates = resourceTemplateRepository.findByDesignerId(designerId, pageable);
        }
        
        List<DesignerProductDTO> productDTOs = templates.getContent().stream()
                .map(template -> convertToProductDTO(template, designerId))
                .collect(Collectors.toList());

        return PagedResponseDTO.<DesignerProductDTO>builder()
                .content(productDTOs)
                .page(page)
                .size(size)
                .totalElements(templates.getTotalElements())
                .totalPages(templates.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DesignerProductDTO getProductDetail(Long resourceTemplateId, Long designerId) {
        ResourceTemplate template = resourceTemplateRepository.findById(resourceTemplateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // Verify ownership
        if (!template.getDesignerId().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view this product");
        }

        return convertToProductDTO(template, designerId);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceTemplateVersionDTO getVersionDetail(Long versionId, Long designerId) {
        ResourceTemplateVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Version not found"));

        // Verify ownership
        if (!version.getCreatedBy().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view this version");
        }

        ResourceTemplateVersionDTO dto = orderMapper.toVersionDto(version);
        populateVersionStatistics(dto);
        return dto;
    }

    @Override
    public ResourceTemplateVersionDTO createNewVersion(Long resourceTemplateId, Long designerId, CreateResourceVersionDTO dto) {
        // 1. Lấy template hiện tại
        ResourceTemplate template = resourceTemplateRepository.findById(resourceTemplateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // Verify ownership
        if (!template.getDesignerId().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to edit this product");
        }

        // 2. Tính version number tiếp theo
        Optional<ResourceTemplateVersion> lastVersion = versionRepository.findLastVersionByTemplateId(resourceTemplateId);
        String nextVersionNumber = calculateNextVersionNumber(lastVersion);

        // 3. Tạo version entity mới
        ResourceTemplateVersion newVersion = ResourceTemplateVersion.builder()
                .templateId(resourceTemplateId)
                .versionNumber(nextVersionNumber)
                .name(dto.getName() != null ? dto.getName() : template.getName())
                .description(dto.getDescription() != null ? dto.getDescription() : template.getDescription())
                .type(dto.getType() != null ? ResourceTemplate.TemplateType.valueOf(dto.getType().toUpperCase()) : template.getType())
                .price(dto.getPrice() != null ? dto.getPrice() : template.getPrice())
                .expiredTime(dto.getExpiredTime())
                .releaseDate(dto.getReleaseDate() != null ? dto.getReleaseDate() : LocalDate.now())
                .status(ResourceTemplate.TemplateStatus.PENDING_REVIEW)
                .createdBy(designerId)
                .build();

        // 4. Xử lý source (upload file hoặc select project)
        if ("PROJECT".equalsIgnoreCase(dto.getSourceType()) && dto.getProjectId() != null) {
            // Load từ project - gọi project service
            try {
                log.info("Loading project {} for version", dto.getProjectId());
                // Project data sẽ được xử lý trong mapper
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project cannot be loaded. Please check the project status.");
            }
        }

        // 5. Gắn images và items
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<ResourceTemplateVersionImage> images = dto.getImages().stream()
                    .map(imgDto -> ResourceTemplateVersionImage.builder()
                            .version(newVersion)
                            .imageUrl(imgDto.getImageUrl())
                            .isThumbnail(imgDto.getIsThumbnail() != null ? imgDto.getIsThumbnail() : false)
                            .build())
                    .collect(Collectors.toList());
            newVersion.setImages(images);
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            List<ResourceTemplateVersionItem> items = dto.getItems().stream()
                    .map(itemDto -> ResourceTemplateVersionItem.builder()
                            .version(newVersion)
                            .itemIndex(itemDto.getItemIndex())
                            .itemUrl(itemDto.getItemUrl())
                            .imageUrl(itemDto.getImageUrl())
                            .build())
                    .collect(Collectors.toList());
            newVersion.setItems(items);
        }

        // 6. Lưu version mới
        ResourceTemplateVersion saved = versionRepository.save(newVersion);

        log.info("Created new version {} for template {} by designer {}", nextVersionNumber, resourceTemplateId, designerId);

        return orderMapper.toVersionDto(saved);
    }

    @Override
    public ResourceTemplateVersionDTO updateVersion(Long versionId, Long designerId, CreateResourceVersionDTO dto) {
        ResourceTemplateVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Version not found"));

        // Verify ownership
        if (!version.getCreatedBy().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to edit this version");
        }

        // Chỉ có thể edit nếu status là PENDING_REVIEW
        if (!version.getStatus().equals(ResourceTemplate.TemplateStatus.PENDING_REVIEW)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "You can only edit versions in PENDING_REVIEW status");
        }

        // Update metadata
        if (dto.getName() != null) version.setName(dto.getName());
        if (dto.getDescription() != null) version.setDescription(dto.getDescription());
        if (dto.getType() != null) {
            version.setType(ResourceTemplate.TemplateType.valueOf(dto.getType().toUpperCase()));
        }
        if (dto.getPrice() != null) version.setPrice(dto.getPrice());
        if (dto.getExpiredTime() != null) version.setExpiredTime(dto.getExpiredTime());
        if (dto.getReleaseDate() != null) version.setReleaseDate(dto.getReleaseDate());

        // Update images
        if (dto.getImages() != null) {
            version.getImages().clear();
            List<ResourceTemplateVersionImage> images = dto.getImages().stream()
                    .map(imgDto -> ResourceTemplateVersionImage.builder()
                            .version(version)
                            .imageUrl(imgDto.getImageUrl())
                            .isThumbnail(imgDto.getIsThumbnail() != null ? imgDto.getIsThumbnail() : false)
                            .build())
                    .collect(Collectors.toList());
            version.setImages(images);
        }

        // Update items
        if (dto.getItems() != null) {
            version.getItems().clear();
            List<ResourceTemplateVersionItem> items = dto.getItems().stream()
                    .map(itemDto -> ResourceTemplateVersionItem.builder()
                            .version(version)
                            .itemIndex(itemDto.getItemIndex())
                            .itemUrl(itemDto.getItemUrl())
                            .imageUrl(itemDto.getImageUrl())
                            .build())
                    .collect(Collectors.toList());
            version.setItems(items);
        }

        ResourceTemplateVersion updated = versionRepository.save(version);
        log.info("Updated version {} by designer {}", versionId, designerId);

        return orderMapper.toVersionDto(updated);
    }

    @Override
    public DesignerProductDTO archiveProduct(Long resourceTemplateId, Long designerId) {
        ResourceTemplate template = resourceTemplateRepository.findById(resourceTemplateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!template.getDesignerId().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to archive this product");
        }

        // State Machine: Chỉ có thể archive từ PUBLISHED state
        if (!template.getStatus().equals(ResourceTemplate.TemplateStatus.PUBLISHED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Only PUBLISHED products can be archived. Current status: " + template.getStatus());
        }

        template.setStatus(ResourceTemplate.TemplateStatus.ARCHIVED);
        ResourceTemplate updated = resourceTemplateRepository.save(template);

        log.info("Archived product {} by designer {} - Status changed from PUBLISHED to ARCHIVED", resourceTemplateId, designerId);

        return convertToProductDTO(updated, designerId);
    }

    @Override
    public DesignerProductDTO unarchiveProduct(Long resourceTemplateId, Long designerId) {
        ResourceTemplate template = resourceTemplateRepository.findById(resourceTemplateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!template.getDesignerId().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to unarchive this product");
        }

        // State Machine: Chỉ có thể unarchive từ ARCHIVED state
        if (!template.getStatus().equals(ResourceTemplate.TemplateStatus.ARCHIVED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Only ARCHIVED products can be unarchived. Current status: " + template.getStatus());
        }

        template.setStatus(ResourceTemplate.TemplateStatus.PUBLISHED);
        ResourceTemplate updated = resourceTemplateRepository.save(template);

        log.info("Unarchived product {} by designer {} - Status changed from ARCHIVED to PUBLISHED", resourceTemplateId, designerId);

        return convertToProductDTO(updated, designerId);
    }

    @Override
    public ResourceTemplateVersionDTO republishVersion(Long versionId, Long designerId) {
        ResourceTemplateVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Version not found"));

        if (!version.getCreatedBy().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to republish this version");
        }

        if (!version.getStatus().equals(ResourceTemplate.TemplateStatus.PENDING_REVIEW)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only PENDING_REVIEW versions can be republished");
        }

        // ✅ FIX: Thêm timestamp để track resubmit
        version.setUpdatedAt(LocalDateTime.now());
        ResourceTemplateVersion updated = versionRepository.save(version);

        log.info("Republished version {} by designer {} at {}", versionId, designerId, LocalDateTime.now());

        return orderMapper.toVersionDto(updated);
    }

    @Override
    public DesignerProductDTO publishVersion(Long versionId, Long designerId) {
        // 1. Tìm version cần publish
        ResourceTemplateVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Version not found"));

        // 2. Kiểm tra quyền sở hữu
        if (!version.getCreatedBy().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You don't have permission to publish this version");
        }

        // 3. Chỉ publish được version đã PUBLISHED (đã được admin approve)
        if (!version.getStatus().equals(ResourceTemplate.TemplateStatus.PUBLISHED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Only PUBLISHED versions (approved by admin) can be set as active");
        }

        // 4. Tìm template chính
        ResourceTemplate template = resourceTemplateRepository.findById(version.getTemplateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 5. Verify ownership template
        if (!template.getDesignerId().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You don't have permission to manage this product");
        }

        // 6. Kiểm tra product không bị archive (state machine check)
        if (template.getStatus().equals(ResourceTemplate.TemplateStatus.ARCHIVED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Cannot publish version for archived product. Please unarchive the product first");
        }

        // 7. Set version này làm version hiện tại
        template.setCurrentPublishedVersionId(versionId);
        template.setStatus(ResourceTemplate.TemplateStatus.PUBLISHED);
        
        // 8. Sync metadata từ version lên template (optional - tùy business logic)
        template.setName(version.getName());
        template.setDescription(version.getDescription());
        template.setType(version.getType());
        template.setPrice(version.getPrice());
        template.setExpiredTime(version.getExpiredTime());
        template.setReleaseDate(version.getReleaseDate());
        
        ResourceTemplate updated = resourceTemplateRepository.save(template);

        log.info("Published version {} for template {} by designer {}", versionId, template.getTemplateId(), designerId);

        return convertToProductDTO(updated, designerId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ResourceTemplateVersionDTO> getProductVersions(
            Long resourceTemplateId, Long designerId, int page, int size) {
        
        // Verify product ownership
        ResourceTemplate template = resourceTemplateRepository.findById(resourceTemplateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!template.getDesignerId().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view this product");
        }

        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResourceTemplateVersion> versionPage = versionRepository.findByTemplateIdOrderByCreatedAtDesc(resourceTemplateId, pageable);

        List<ResourceTemplateVersionDTO> dtos = versionPage.getContent().stream()
                .map(v -> {
                    ResourceTemplateVersionDTO dto = orderMapper.toVersionDto(v);
                    populateVersionStatistics(dto);
                    return dto;
                })
                .collect(Collectors.toList());

        return PagedResponseDTO.<ResourceTemplateVersionDTO>builder()
                .content(dtos)
                .page(page)
                .size(size)
                .totalElements(versionPage.getTotalElements())
                .totalPages(versionPage.getTotalPages())
                .build();
    }

    @Override
    public void deleteVersion(Long versionId, Long designerId) {
        ResourceTemplateVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Version not found"));

        if (!version.getCreatedBy().equals(designerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this version");
        }

        // Khong dc xoa resource co status PUBLISHED
        if (version.getStatus().equals(ResourceTemplate.TemplateStatus.PUBLISHED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "You can not delete versions in PUBLISHED status");
        }

        versionRepository.deleteById(versionId);
        log.info("Deleted version {} by designer {}", versionId, designerId);
    }

    // ==================== Helper methods ====================

    private DesignerProductDTO convertToProductDTO(ResourceTemplate template, Long designerId) {
        // Lấy tất cả versions
        List<ResourceTemplateVersion> versions = versionRepository.findByTemplateIdOrderByCreatedAtDesc(template.getTemplateId());

        // Lấy version PUBLISHED hiện tại dựa trên currentPublishedVersionId của template
        // Không dùng findFirst() vì có thể có nhiều version PUBLISHED, 
        // cần lấy đúng version mà designer đã chọn làm current
        Long currentPublishedVersionId = template.getCurrentPublishedVersionId();
        Optional<ResourceTemplateVersion> publishedVersion = versions.stream()
                .filter(v -> v.getVersionId().equals(currentPublishedVersionId))
                .findFirst();

        // Tính statistics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        BigDecimal totalRevenue = dashboardRepository.totalRevenueForDesigner(designerId, thirtyDaysAgo, now);
        List<Object[]> sales = dashboardRepository.salesByDay(designerId, thirtyDaysAgo, now);
        long totalPurchases = sales.stream()
                .map(s -> s[1])
                .filter(Objects::nonNull)
                .mapToLong(s -> ((Number) s).longValue())
                .sum();

        List<ResourceTemplateVersionDTO> versionDTOs = versions.stream()
                .map(v -> {
                    ResourceTemplateVersionDTO dto = orderMapper.toVersionDto(v);
                    populateVersionStatistics(dto);
                    return dto;
                })
                .collect(Collectors.toList());

        // Prefer images from the published version; fall back to template or latest version images
        List<ResourceImageDTO> productImages = orderMapper.mapImages(template.getImages());
        if ((productImages == null || productImages.isEmpty()) && publishedVersion.isPresent()) {
            productImages = orderMapper.mapVersionImages(publishedVersion.get().getImages());
        }
        if ((productImages == null || productImages.isEmpty()) && !versions.isEmpty()) {
            productImages = orderMapper.mapVersionImages(versions.get(0).getImages());
        }

        String bannerImageUrl = null;
        if (productImages != null && !productImages.isEmpty()) {
            bannerImageUrl = productImages.stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                    .map(ResourceImageDTO::getImageUrl)
                    .findFirst()
                    .orElse(productImages.get(0).getImageUrl());
        }

        return DesignerProductDTO.builder()
                .resourceTemplateId(template.getTemplateId())
                .designerId(template.getDesignerId())
                .name(template.getName())
                .description(template.getDescription())
                .type(template.getType() != null ? template.getType().name() : null)
                .price(template.getPrice())
                .status(template.getStatus() != null ? template.getStatus().name() : null) // State Machine status
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .images(productImages)
                .bannerImageUrl(bannerImageUrl)
                .totalPurchases(totalPurchases)
                .totalRevenue(totalRevenue)
            .avgResourceRating(0.0)
                .currentPublishedVersionId(publishedVersion.map(ResourceTemplateVersion::getVersionId).orElse(null))
                .currentVersionNumber(publishedVersion.map(ResourceTemplateVersion::getVersionNumber).orElse(null))
                .versions(versionDTOs)
                .build();
    }

    private void populateVersionStatistics(ResourceTemplateVersionDTO dto) {
        // TODO: Implement statistics population from dashboard
        dto.setPurchaseCount(0L);
        dto.setFeedbackCount(0L);
        dto.setAverageRating(0.0);
    }

    private String calculateNextVersionNumber(Optional<ResourceTemplateVersion> lastVersion) {
        if (lastVersion.isEmpty()) {
            return "1.0";
        }

        String currentVersion = lastVersion.get().getVersionNumber();
        String[] parts = currentVersion.split("\\.");
        
        if (parts.length == 2) {
            int major = Integer.parseInt(parts[0]);
            return (major + 1) + ".0";
        }

        return "1.0";
    }
}
