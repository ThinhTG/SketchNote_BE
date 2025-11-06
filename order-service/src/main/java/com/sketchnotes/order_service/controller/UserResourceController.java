package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.entity.UserResource;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.service.UserResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/user_resources")
@RequiredArgsConstructor
public class UserResourceController {
    private final UserResourceService userResourceService;
    private final IdentityClient identityClient;

    /**
     * 📦 [GET] Lấy tất cả resource mà user đang sở hữu (library của họ)
     */
    @GetMapping("/user/me")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<UserResource>>> getUserResources() {
        var user = identityClient.getCurrentUser();
        List<UserResource> resources = userResourceService.getUserResources(user.getResult().getId());
        return ResponseEntity.ok(ApiResponse.success(resources, "Fetched user resources"));
    }

    /**
     * 📦 [GET] Lấy danh sách ResourceTemplate mà user đã mua (bao gồm các itemUrl)
     */
    @GetMapping("/user/me/templates")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ResourceTemplateDTO>>> getMyPurchasedTemplates() {
        var user = identityClient.getCurrentUser();
        List<ResourceTemplateDTO> templates = userResourceService.getPurchasedTemplates(user.getResult().getId());
        return ResponseEntity.ok(ApiResponse.success(templates, "Fetched purchased templates"));
    }

    /**
     * 🛒 [POST] Thêm mới user_resource (sử dụng khi test hoặc admin muốn thêm thủ công)
     * Trong thực tế, Kafka consumer sẽ tạo tự động sau khi payment success.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResource>> createUserResource(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam Long resourceTemplateId
    ) {
        UserResource newResource = userResourceService.createUserResource(orderId, userId, resourceTemplateId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newResource, "User resource created"));
    }

}
