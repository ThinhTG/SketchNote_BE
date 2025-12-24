package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.dtos.ResourceItemDTO;
import com.sketchnotes.order_service.dtos.ResourceImageDTO;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.dtos.designer.ResourceTemplateVersionDTO;
import com.sketchnotes.order_service.dtos.PurchasedTemplateDTO;
import com.sketchnotes.order_service.entity.*;
import com.sketchnotes.order_service.repository.ResourceImageRepository;
import com.sketchnotes.order_service.repository.UserResourceRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateVersionRepository;
import com.sketchnotes.order_service.service.UserResourceService;
import com.sketchnotes.order_service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserResourceServiceImpl implements UserResourceService {

   private final UserResourceRepository userResourceRepository;
   private final ResourceTemplateRepository resourceTemplateRepository;
   private final ResourceImageRepository imageRepository;
   private final ResourceTemplateVersionRepository versionRepository;
   private final OrderMapper orderMapper;

    @Override
    @Transactional
    public UserResource createUserResource(Long orderId, Long userId, Long resourceTemplateId) {
        Optional<UserResource> existingOpt = userResourceRepository
                .findFirstByUserIdAndResourceTemplateId(userId, resourceTemplateId);

        // Get current published version ID from template
        ResourceTemplate template = resourceTemplateRepository.findById(resourceTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("Resource template not found: " + resourceTemplateId));
        Long currentVersionId = template.getCurrentPublishedVersionId();

        if (existingOpt.isPresent()) {
            UserResource existing = existingOpt.get();
            if (existing.isActive()) {
                // Đã sở hữu và đang active
                throw new IllegalStateException("User already owns this resource template.");
            }
            // Tài nguyên tồn tại nhưng inactive -> kích hoạt lại
            existing.setActive(true);
            existing.setOrderId(orderId);
            existing.setPurchasedVersionId(currentVersionId); // Update to current version
            existing.setCurrentVersionId(currentVersionId); // Set current version to purchased version
            existing.setUpdatedAt(LocalDateTime.now());
            log.info("Reactivated UserResource for user {} template {} with version {}", userId, resourceTemplateId, currentVersionId);
            return userResourceRepository.save(existing);
        }

        UserResource userResource = UserResource.builder()
                .orderId(orderId)
                .userId(userId)
                .resourceTemplateId(resourceTemplateId)
                .purchasedVersionId(currentVersionId) // Save the version user purchased
                .currentVersionId(currentVersionId) // Initially, current version equals purchased version
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Created UserResource for user {} template {} with version {}", userId, resourceTemplateId, currentVersionId);
        return userResourceRepository.save(userResource);
    }

    @Override
    public List<UserResource> getUserResources(Long userId) {
        return userResourceRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    public Page<UserResource> getUserResources(Long userId, Pageable pageable) {
        return userResourceRepository.findByUserIdAndActiveTrue(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Deprecated
    public List<ResourceTemplateDTO> getPurchasedTemplates(Long userId) {
        // 🔹 Use Set to avoid duplicates (in case user owns and also purchased their own template)
        Set<Long> templateIdsSet = new HashSet<>();
        
        // 🔹 1. Get all resource template IDs that user has purchased
        List<Long> purchasedTemplateIds = userResourceRepository.findActiveTemplateIdsByUserId(userId);
        if (purchasedTemplateIds != null && !purchasedTemplateIds.isEmpty()) {
            templateIdsSet.addAll(purchasedTemplateIds);
        }
        
        // 🔹 2. Get all template IDs that user owns (designerId == userId)
        // Only get PUBLISHED templates that the user created
        List<ResourceTemplate> ownedTemplates = resourceTemplateRepository
                .findByDesignerIdAndStatus(userId, ResourceTemplate.TemplateStatus.PUBLISHED);
        if (ownedTemplates != null && !ownedTemplates.isEmpty()) {
            for (ResourceTemplate template : ownedTemplates) {
                templateIdsSet.add(template.getTemplateId());
            }
        }
        
        // 🔹 If no templates found from both sources, return empty list
        if (templateIdsSet.isEmpty()) return java.util.Collections.emptyList();

        // 🔹 Convert Set to List for repository query
        List<Long> templateIds = new ArrayList<>(templateIdsSet);

        // 🔹 Get templates - only PUBLISHED ones
        List<ResourceTemplate> templates = resourceTemplateRepository
                .findByTemplateIdInAndStatus(templateIds, ResourceTemplate.TemplateStatus.PUBLISHED);

        List<ResourceTemplateDTO> result = new ArrayList<>();
        for (ResourceTemplate rt : templates) {
            // 🔹 IMPORTANT: Always return the CURRENT PUBLISHED VERSION
            // When user purchases version 1.0, they automatically get upgraded to version 2.0 when it's published
            // This is done by using currentPublishedVersionId to fetch the latest version data
           List<ResourcesTemplateImage> images = imageRepository.findByResourceTemplateAndIsThumbnailTrue(rt)
                    .orElse(null);
            ResourceTemplateDTO dto = ResourceTemplateDTO.builder()
                    .resourceTemplateId(rt.getTemplateId())
                    .designerId(rt.getDesignerId())
                    .name(rt.getName())
                    .description(rt.getDescription())
                    .isOwner(rt.getDesignerId().equals(userId))
                    .type(rt.getType() != null ? rt.getType().name() : null)
                    .price(rt.getPrice())
                    .bannerUrl((images != null && !images.isEmpty() ? images.get(0).getImageUrl() : null))
                    .releaseDate(rt.getReleaseDate())
                    .createdAt(rt.getCreatedAt())
                    .updatedAt(rt.getUpdatedAt())
                    .status(rt.getStatus() != null ? rt.getStatus().name() : null)
                    .build();

            // 🔹 Get items from CURRENT VERSION (if currentPublishedVersionId exists)
            // Otherwise fall back to template items (for backward compatibility)
            if (rt.getItems() != null) {
                java.util.List<ResourceItemDTO> itemDTOs = new java.util.ArrayList<>();
                for (ResourceTemplateItem item : rt.getItems()) {
                    itemDTOs.add(ResourceItemDTO.builder()
                            .resourceItemId(item.getResourceItemId())
                            .itemIndex(item.getItemIndex())
                            .itemUrl(item.getItemUrl())
                            .imageUrl(item.getImageUrl())
                            .build());
                }
                dto.setItems(itemDTOs);
            }


            // Images are optional; if needed, map to ResourceImageDTO here.
            result.add(dto);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchasedTemplateDTO> getPurchasedTemplatesWithVersions(Long userId) {
        // 1. Get all user resources with purchased version info
        List<UserResource> userResources = userResourceRepository.findActiveResourcesWithVersionByUserId(userId);
        if (userResources == null || userResources.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 2. Collect template IDs
        List<Long> templateIds = userResources.stream()
                .map(UserResource::getResourceTemplateId)
                .collect(Collectors.toList());

        // 3. Get templates - only PUBLISHED ones
        List<ResourceTemplate> templates = resourceTemplateRepository
                .findByTemplateIdInAndStatus(templateIds, ResourceTemplate.TemplateStatus.PUBLISHED);

        // 4. Create a map for quick lookup of userResource by templateId
        java.util.Map<Long, UserResource> userResourceMap = userResources.stream()
                .collect(Collectors.toMap(UserResource::getResourceTemplateId, ur -> ur, (a, b) -> a));

        // 5. Get all published versions for these templates
        List<ResourceTemplateVersion> allVersions = versionRepository.findPublishedVersionsByTemplateIds(templateIds);
        
        // Group versions by templateId
        java.util.Map<Long, List<ResourceTemplateVersion>> versionsByTemplate = allVersions.stream()
                .collect(Collectors.groupingBy(ResourceTemplateVersion::getTemplateId));

        List<PurchasedTemplateDTO> result = new ArrayList<>();
        
        for (ResourceTemplate template : templates) {
            UserResource userResource = userResourceMap.get(template.getTemplateId());
            if (userResource == null) continue;

            Long purchasedVersionId = userResource.getPurchasedVersionId();
            Long userCurrentVersionId = userResource.getCurrentVersionId(); // Version user is currently using
            List<ResourceTemplateVersion> templateVersions = versionsByTemplate.getOrDefault(template.getTemplateId(), new ArrayList<>());
            
            // Filter versions: user can access purchased version + all newer versions
            List<ResourceTemplateVersion> accessibleVersions = filterAccessibleVersions(templateVersions, purchasedVersionId);
            
            // Get purchased version info
            ResourceTemplateVersion purchasedVersion = templateVersions.stream()
                    .filter(v -> v.getVersionId().equals(purchasedVersionId))
                    .findFirst()
                    .orElse(null);
            
            // Get user's current version (what they are using now)
            ResourceTemplateVersion userCurrentVersion = templateVersions.stream()
                    .filter(v -> v.getVersionId().equals(userCurrentVersionId))
                    .findFirst()
                    .orElse(purchasedVersion); // fallback to purchased version if currentVersionId is null
            
            // Get latest published version - USE template.getCurrentPublishedVersionId() as the SINGLE SOURCE OF TRUTH
            // This is updated when staff approves a new version in reviewVersion()
            Long latestPublishedVersionId = template.getCurrentPublishedVersionId();
            ResourceTemplateVersion latestVersion = null;
            if (latestPublishedVersionId != null) {
                latestVersion = templateVersions.stream()
                        .filter(v -> v.getVersionId().equals(latestPublishedVersionId))
                        .findFirst()
                        .orElse(null);
                // If not found in list (shouldn't happen but fallback), try to get from last in list
                if (latestVersion == null && !templateVersions.isEmpty()) {
                    latestVersion = templateVersions.get(templateVersions.size() - 1);
                }
            } else if (!templateVersions.isEmpty()) {
                // Fallback for legacy templates without currentPublishedVersionId
                latestVersion = templateVersions.get(templateVersions.size() - 1);
            }
            
            // Determine if there's a newer version available for upgrade
            // User's effective current version is: currentVersionId if set, otherwise purchasedVersionId
            Long userEffectiveVersionId = userCurrentVersionId != null ? userCurrentVersionId : purchasedVersionId;
            boolean hasNewerVersion = false;
            
            if (latestPublishedVersionId != null && userEffectiveVersionId != null) {
                // Compare using the authoritative latestPublishedVersionId from template
                hasNewerVersion = !latestPublishedVersionId.equals(userEffectiveVersionId);
            } else if (latestVersion != null && userEffectiveVersionId != null) {
                // Fallback to version object comparison
                hasNewerVersion = !latestVersion.getVersionId().equals(userEffectiveVersionId);
            }
            
            // Build DTO
            PurchasedTemplateDTO dto = PurchasedTemplateDTO.builder()
                    .resourceTemplateId(template.getTemplateId())
                    .designerId(template.getDesignerId())
                    .name(template.getName())
                    .description(template.getDescription())
                    .type(template.getType() != null ? template.getType().name() : null)
                    .price(template.getPrice())
                    .releaseDate(template.getReleaseDate())
                    .createdAt(template.getCreatedAt())
                    .updatedAt(template.getUpdatedAt())
                    .status(template.getStatus() != null ? template.getStatus().name() : null)
                    .purchasedVersionId(purchasedVersionId)
                    .purchasedVersionNumber(purchasedVersion != null ? purchasedVersion.getVersionNumber() : null)
                    .currentVersionId(userCurrentVersion != null ? userCurrentVersion.getVersionId() : null)
                    .currentVersionNumber(userCurrentVersion != null ? userCurrentVersion.getVersionNumber() : null)
                    .latestVersionId(latestVersion != null ? latestVersion.getVersionId() : null)
                    .latestVersionNumber(latestVersion != null ? latestVersion.getVersionNumber() : null)
                    .hasNewerVersion(hasNewerVersion)
                    .availableVersions(accessibleVersions.stream()
                            .map(orderMapper::toVersionDto)
                            .collect(Collectors.toList()))
                    .build();

            // Set items and images from user's CURRENT version (what they are using)
            if (userCurrentVersion != null) {
                dto.setItems(userCurrentVersion.getItems().stream()
                        .map(item -> ResourceItemDTO.builder()
                                .itemIndex(item.getItemIndex())
                                .itemUrl(item.getItemUrl())
                                .imageUrl(item.getImageUrl())
                                .build())
                        .collect(Collectors.toList()));
                
                dto.setImages(userCurrentVersion.getImages().stream()
                        .map(img -> ResourceImageDTO.builder()
                                .imageUrl(img.getImageUrl())
                                .isThumbnail(img.getIsThumbnail())
                                .build())
                        .collect(Collectors.toList()));
            }

            result.add(dto);
        }
        
        return result;
    }

    /**
     * Filter versions that user can access: purchased version + all newer versions
     */
    private List<ResourceTemplateVersion> filterAccessibleVersions(
            List<ResourceTemplateVersion> allVersions, 
            Long purchasedVersionId) {
        
        if (purchasedVersionId == null || allVersions.isEmpty()) {
            // If no purchased version recorded (legacy data), return all versions
            return allVersions;
        }
        
        // Find the index of purchased version
        int purchasedIndex = -1;
        for (int i = 0; i < allVersions.size(); i++) {
            if (allVersions.get(i).getVersionId().equals(purchasedVersionId)) {
                purchasedIndex = i;
                break;
            }
        }
        
        if (purchasedIndex == -1) {
            // Purchased version not found (maybe unpublished), return all versions
            return allVersions;
        }
        
        // Return purchased version + all newer versions
        return allVersions.subList(purchasedIndex, allVersions.size());
    }
    
    @Override
    public UserResource getUserResourceByUserIdAndResourceId(Long userId, Long resourceId) {
        return userResourceRepository.findByUserIdAndResourceTemplateIdAndActiveTrue(userId, resourceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User %d has not purchased resource %d or it is not active", userId, resourceId)));
    }
    
    @Override
    @Transactional
    public UserResource upgradeToLatestVersion(Long userId, Long resourceTemplateId) {
        // 1. Find user's resource
        UserResource userResource = userResourceRepository.findByUserIdAndResourceTemplateIdAndActiveTrue(userId, resourceTemplateId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User %d has not purchased resource %d or it is not active", userId, resourceTemplateId)));
        
        // 2. Get the resource template to find the latest published version
        ResourceTemplate template = resourceTemplateRepository.findById(resourceTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("Resource template not found: " + resourceTemplateId));
        
        Long latestVersionId = template.getCurrentPublishedVersionId();
        if (latestVersionId == null) {
            throw new IllegalStateException("No published version available for this resource");
        }
        
        // 3. Check if user is already on the latest version
        Long currentVersionId = userResource.getCurrentVersionId();
        if (currentVersionId != null && currentVersionId.equals(latestVersionId)) {
            throw new IllegalStateException("User is already using the latest version");
        }
        
        // 4. Verify the latest version exists and is PUBLISHED
        ResourceTemplateVersion latestVersion = versionRepository.findById(latestVersionId)
                .orElseThrow(() -> new IllegalStateException("Latest version not found"));
        
        if (!ResourceTemplate.TemplateStatus.PUBLISHED.equals(latestVersion.getStatus())) {
            throw new IllegalStateException("Latest version is not published");
        }
        
        // 5. Upgrade the user's resource to the latest version (free upgrade)
        userResource.setCurrentVersionId(latestVersionId);
        userResource.setUpdatedAt(LocalDateTime.now());
        
        log.info("User {} upgraded resource {} from version {} to version {}", 
                userId, resourceTemplateId, currentVersionId, latestVersionId);
        
        return userResourceRepository.save(userResource);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasNewerVersionAvailable(Long userId, Long resourceTemplateId) {
        // 1. Find user's resource
        Optional<UserResource> userResourceOpt = userResourceRepository
                .findByUserIdAndResourceTemplateIdAndActiveTrue(userId, resourceTemplateId);
        
        if (userResourceOpt.isEmpty()) {
            return false;
        }
        
        UserResource userResource = userResourceOpt.get();
        
        // 2. Get the resource template
        Optional<ResourceTemplate> templateOpt = resourceTemplateRepository.findById(resourceTemplateId);
        if (templateOpt.isEmpty()) {
            return false;
        }
        
        ResourceTemplate template = templateOpt.get();
        Long latestVersionId = template.getCurrentPublishedVersionId();
        
        if (latestVersionId == null) {
            return false;
        }
        
        // 3. Determine user's effective current version
        // Priority: currentVersionId > purchasedVersionId
        Long userEffectiveVersionId = userResource.getCurrentVersionId();
        if (userEffectiveVersionId == null) {
            userEffectiveVersionId = userResource.getPurchasedVersionId();
        }
        
        // 4. If user has no version info at all (legacy data), they can upgrade
        if (userEffectiveVersionId == null) {
            return true;
        }
        
        // 5. Check if user's effective version is different from latest
        return !userEffectiveVersionId.equals(latestVersionId);
    }
}
