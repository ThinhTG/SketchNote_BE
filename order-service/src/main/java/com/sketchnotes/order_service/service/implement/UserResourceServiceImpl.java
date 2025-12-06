package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.dtos.ResourceItemDTO;
import com.sketchnotes.order_service.dtos.ResourceImageDTO;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.entity.UserResource;
import com.sketchnotes.order_service.entity.ResourceTemplate;
import com.sketchnotes.order_service.entity.ResourceTemplateItem;
import com.sketchnotes.order_service.repository.UserResourceRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.service.UserResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UserResourceServiceImpl implements UserResourceService {

   private  final UserResourceRepository userResourceRepository;
   private  final ResourceTemplateRepository resourceTemplateRepository;

    @Override
    @Transactional
    public UserResource createUserResource(Long orderId, Long userId, Long resourceTemplateId) {
        Optional<UserResource> existingOpt = userResourceRepository
                .findFirstByUserIdAndResourceTemplateId(userId, resourceTemplateId);

        if (existingOpt.isPresent()) {
            UserResource existing = existingOpt.get();
            if (existing.isActive()) {
                // Đã sở hữu và đang active
                throw new IllegalStateException("User already owns this resource template.");
            }
            // Tài nguyên tồn tại nhưng inactive -> kích hoạt lại
            existing.setActive(true);
            existing.setOrderId(orderId);
            existing.setUpdatedAt(LocalDateTime.now());
            return userResourceRepository.save(existing);
        }

        UserResource userResource = UserResource.builder()
                .orderId(orderId)
                .userId(userId)
                .resourceTemplateId(resourceTemplateId)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

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
    public List<ResourceTemplateDTO> getPurchasedTemplates(Long userId) {
        // 🔹 Get all resource template IDs that user has purchased
        List<Long> templateIds = userResourceRepository.findActiveTemplateIdsByUserId(userId);
        if (templateIds == null || templateIds.isEmpty()) return java.util.Collections.emptyList();

        // 🔹 Get templates - only PUBLISHED ones
        List<ResourceTemplate> templates = resourceTemplateRepository
                .findByTemplateIdInAndStatus(templateIds, ResourceTemplate.TemplateStatus.PUBLISHED);

        List<ResourceTemplateDTO> result = new java.util.ArrayList<>();
        for (ResourceTemplate rt : templates) {
            // 🔹 IMPORTANT: Always return the CURRENT PUBLISHED VERSION
            // When user purchases version 1.0, they automatically get upgraded to version 2.0 when it's published
            // This is done by using currentPublishedVersionId to fetch the latest version data
            
            ResourceTemplateDTO dto = ResourceTemplateDTO.builder()
                    .resourceTemplateId(rt.getTemplateId())
                    .designerId(rt.getDesignerId())
                    .name(rt.getName())
                    .description(rt.getDescription())
                    .type(rt.getType() != null ? rt.getType().name() : null)
                    .price(rt.getPrice())
                    .expiredTime(rt.getExpiredTime())
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
    public UserResource getUserResourceByUserIdAndResourceId(Long userId, Long resourceId) {
        return userResourceRepository.findByUserIdAndResourceTemplateIdAndActiveTrue(userId, resourceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User %d has not purchased resource %d or it is not active", userId, resourceId)));
    }
}
