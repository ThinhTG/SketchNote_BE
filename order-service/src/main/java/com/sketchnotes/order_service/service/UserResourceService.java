package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.entity.UserResource;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.dtos.PurchasedTemplateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserResourceService {
    /**
     * Tạo mới một UserResource sau khi thanh toán thành công.
     */
    UserResource createUserResource(Long orderId, Long userId, Long resourceTemplateId);

    /**
     * Lấy toàn bộ resource mà user đang sở hữu.
     */
    List<UserResource> getUserResources(Long userId);

    /**
     * Lấy resource của user theo phân trang.
     */
    Page<UserResource> getUserResources(Long userId, Pageable pageable);

    /**
     * Lấy danh sách ResourceTemplate mà user đã mua (bao gồm items có itemUrl)
     * @deprecated Use getPurchasedTemplatesWithVersions for better version support
     */
    @Deprecated
    java.util.List<ResourceTemplateDTO> getPurchasedTemplates(Long userId);
    
    /**
     * Lấy danh sách ResourceTemplate mà user đã mua với thông tin version đầy đủ.
     * User sẽ có quyền truy cập:
     * - Version đã mua (purchasedVersionId)
     * - Tất cả các version mới hơn (free upgrade)
     */
    List<PurchasedTemplateDTO> getPurchasedTemplatesWithVersions(Long userId);
    
    /**
     * Get user resource by userId and resourceId
     * Used by identity-service to validate feedback eligibility
     */
    UserResource getUserResourceByUserIdAndResourceId(Long userId, Long resourceId);
    
    /**
     * Upgrade user's resource to the latest published version for free.
     * This only updates for the specific user, not globally.
     * No payment or new order is required.
     * 
     * @param userId the user ID
     * @param resourceTemplateId the resource template ID to upgrade
     * @return the updated UserResource with new currentVersionId
     * @throws IllegalArgumentException if user doesn't own the resource
     * @throws IllegalStateException if no newer version is available
     */
    UserResource upgradeToLatestVersion(Long userId, Long resourceTemplateId);
    
    /**
     * Check if there's a newer version available for the user's resource.
     * 
     * @param userId the user ID
     * @param resourceTemplateId the resource template ID
     * @return true if a newer version is available
     */
    boolean hasNewerVersionAvailable(Long userId, Long resourceTemplateId);
    
    /**
     * Lấy danh sách ResourceTemplate mà Designer đã đăng bán (không bao gồm các resource đã mua).
     * Chỉ lấy các resource có status PUBLISHED.
     * 
     * @param designerId the designer's user ID
     * @return list of published templates owned by the designer
     */
    List<ResourceTemplateDTO> getDesignerPublishedTemplates(Long designerId);
}
