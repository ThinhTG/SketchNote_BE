package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.PagedResponseDTO;
import com.sketchnotes.order_service.dtos.UserResponse;
import com.sketchnotes.order_service.dtos.designer.CreateResourceVersionDTO;
import com.sketchnotes.order_service.dtos.designer.DesignerProductDTO;
import com.sketchnotes.order_service.dtos.designer.ResourceTemplateVersionDTO;
import com.sketchnotes.order_service.service.designer.DesignerResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders/designer/products")
@RequiredArgsConstructor
@Slf4j
public class DesignerResourceController {
    
    private final DesignerResourceService designerResourceService;
    private final IdentityClient identityClient;

    /**
     * Lấy danh sách sản phẩm của designer (có phân trang)
     * GET /api/orders/designer/products?page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<DesignerProductDTO>>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long designerId = getCurrentDesignerId();
        PagedResponseDTO<DesignerProductDTO> result = designerResourceService.getMyProducts(
                designerId, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched designer products"));
    }

    /**
     * Lấy chi tiết sản phẩm (bao gồm tất cả versions)
     * GET /api/orders/designer/products/{resourceTemplateId}
     */
    @GetMapping("/{resourceTemplateId}")
    public ResponseEntity<ApiResponse<DesignerProductDTO>> getProductDetail(
            @PathVariable Long resourceTemplateId) {
        
        Long designerId = getCurrentDesignerId();
        DesignerProductDTO product = designerResourceService.getProductDetail(resourceTemplateId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(product, "Fetched product detail"));
    }

    /**
     * Lấy chi tiết một version cụ thể
     * GET /api/orders/designer/products/versions/{versionId}
     */
    @GetMapping("/versions/{versionId}")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> getVersionDetail(
            @PathVariable Long versionId) {
        
        Long designerId = getCurrentDesignerId();
        ResourceTemplateVersionDTO version = designerResourceService.getVersionDetail(versionId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(version, "Fetched version detail"));
    }

    /**
     * Tạo version mới cho sản phẩm
     * POST /api/orders/designer/products/{resourceTemplateId}/versions
     */
    @PostMapping("/{resourceTemplateId}/versions")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> createNewVersion(
            @PathVariable Long resourceTemplateId,
            @RequestBody CreateResourceVersionDTO dto) {
        
        Long designerId = getCurrentDesignerId();
        
        // Validate required fields
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (dto.getPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price is required");
        }
        if (dto.getReleaseDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release date is required and must be >= today");
        }
        if (dto.getExpiredTime() != null && dto.getReleaseDate() != null) {
            if (dto.getExpiredTime().isBefore(dto.getReleaseDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Expiration date must be after the release date");
            }
        }
        
        ResourceTemplateVersionDTO version = designerResourceService.createNewVersion(
                resourceTemplateId, designerId, dto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(version, "New version created and submitted for review"));
    }

    /**
     * Cập nhật metadata của version đang PENDING_REVIEW
     * PUT /api/orders/designer/products/versions/{versionId}
     */
    @PutMapping("/versions/{versionId}")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> updateVersion(
            @PathVariable Long versionId,
            @RequestBody CreateResourceVersionDTO dto) {
        
        Long designerId = getCurrentDesignerId();
        
        // Validate required fields nếu có update
        if (dto.getName() != null && dto.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name cannot be empty");
        }
        if (dto.getPrice() != null && dto.getPrice().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price must be greater than 0");
        }
        
        ResourceTemplateVersionDTO version = designerResourceService.updateVersion(
                versionId, designerId, dto);
        
        return ResponseEntity.ok(ApiResponse.success(version, "Version updated successfully"));
    }

    /**
     * Archive sản phẩm (ngừng bán)
     * POST /api/orders/designer/products/{resourceTemplateId}/archive
     */
    @PostMapping("/{resourceTemplateId}/archive")
    public ResponseEntity<ApiResponse<DesignerProductDTO>> archiveProduct(
            @PathVariable Long resourceTemplateId) {
        
        Long designerId = getCurrentDesignerId();
        DesignerProductDTO product = designerResourceService.archiveProduct(resourceTemplateId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(product, "Product archived successfully"));
    }

    /**
     * Unarchive sản phẩm
     * POST /api/orders/designer/products/{resourceTemplateId}/unarchive
     */
    @PostMapping("/{resourceTemplateId}/unarchive")
    public ResponseEntity<ApiResponse<DesignerProductDTO>> unarchiveProduct(
            @PathVariable Long resourceTemplateId) {
        
        Long designerId = getCurrentDesignerId();
        DesignerProductDTO product = designerResourceService.unarchiveProduct(resourceTemplateId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(product, "Product unarchived successfully"));
    }

    /**
     * Republish sản phẩm (từ PENDING_REVIEW -> PENDING_REVIEW lại)
     * Được sử dụng khi designer muốn resubmit sau khi fix feedback
     * POST /api/orders/designer/products/versions/{versionId}/republish
     */
    @PostMapping("/versions/{versionId}/republish")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> republishVersion(
            @PathVariable Long versionId) {
        
        Long designerId = getCurrentDesignerId();
        ResourceTemplateVersionDTO version = designerResourceService.republishVersion(versionId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(version, "Version resubmitted for review"));
    }

    /**
     * Lấy danh sách versions của một sản phẩm
     * GET /api/orders/designer/products/{resourceTemplateId}/versions?page=0&size=10
     */
    @GetMapping("/{resourceTemplateId}/versions")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateVersionDTO>>> getProductVersions(
            @PathVariable Long resourceTemplateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long designerId = getCurrentDesignerId();
        PagedResponseDTO<ResourceTemplateVersionDTO> result = designerResourceService.getProductVersions(
                resourceTemplateId, designerId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched product versions"));
    }

    /**
     * Xóa version (chỉ xóa được version PENDING_REVIEW)
     * DELETE /api/orders/designer/products/versions/{versionId}
     */
    @DeleteMapping("/versions/{versionId}")
    public ResponseEntity<ApiResponse<String>> deleteVersion(
            @PathVariable Long versionId) {
        
        Long designerId = getCurrentDesignerId();
        designerResourceService.deleteVersion(versionId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Version deleted successfully"));
    }

    // ==================== Helper methods ====================

    /**
     * Lấy designer ID từ current user
     */
    private Long getCurrentDesignerId() {
        try {
            ApiResponse<UserResponse> apiResponse = identityClient.getCurrentUser();
            UserResponse user = apiResponse.getResult();
            
            if (user == null || user.getId() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            
            if (!"DESIGNER".equalsIgnoreCase(user.getRole())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only designers can access this resource");
            }
            
            return user.getId();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to authenticate user");
        }
    }
}
