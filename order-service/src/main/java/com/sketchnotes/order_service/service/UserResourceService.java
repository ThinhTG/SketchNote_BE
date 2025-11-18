package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.entity.UserResource;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
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
     */
    java.util.List<ResourceTemplateDTO> getPurchasedTemplates(Long userId);
}
