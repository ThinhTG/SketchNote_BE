package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.PagedResponseDTO;
import com.sketchnotes.order_service.dtos.UserResponse;
import com.sketchnotes.order_service.dtos.designer.CreateResourceVersionDTO;
import com.sketchnotes.order_service.dtos.designer.DesignerProductDTO;
import com.sketchnotes.order_service.dtos.designer.ResourceTemplateVersionDTO;
import com.sketchnotes.order_service.entity.ResourceTemplate;
import com.sketchnotes.order_service.service.designer.DesignerResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Designer Resource Management", description = "APIs cho Designer quản lý sản phẩm (Resource Templates) và versions")
public class DesignerResourceController {
    
    private final DesignerResourceService designerResourceService;
    private final IdentityClient identityClient;

    @Operation(
        summary = "Lấy danh sách sản phẩm của designer",
        description = "Lấy tất cả sản phẩm (Resource Templates) của designer với phân trang, tìm kiếm và filter. " +
                      "Có thể filter theo status (PUBLISHED, ARCHIVED, PENDING_REVIEW, REJECTED, DELETED) hoặc tìm kiếm theo tên/mô tả."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<DesignerProductDTO>>> getMyProducts(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Số lượng items mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Trường sắp xếp", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Hướng sắp xếp (asc hoặc desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            
            @Parameter(description = "Từ khóa tìm kiếm theo tên hoặc mô tả sản phẩm", example = "icon")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Filter theo status: PUBLISHED, ARCHIVED, PENDING_REVIEW, REJECTED, DELETED. Để trống để lấy tất cả.", example = "ARCHIVED")
            @RequestParam(required = false) ResourceTemplate.TemplateStatus status) {
        
        Long designerId = getCurrentDesignerId();
        PagedResponseDTO<DesignerProductDTO> result = designerResourceService.getMyProducts(
                designerId, page, size, sortBy, sortDir, search, status);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched designer products"));
    }

    @Operation(
        summary = "Lấy chi tiết sản phẩm",
        description = "Lấy thông tin chi tiết của một sản phẩm bao gồm tất cả versions, thống kê doanh thu và lượt mua"
    )
    @GetMapping("/{resourceTemplateId}")
    public ResponseEntity<ApiResponse<DesignerProductDTO>> getProductDetail(
            @Parameter(description = "ID của sản phẩm (Resource Template)", required = true, example = "1")
            @PathVariable Long resourceTemplateId) {
        
        Long designerId = getCurrentDesignerId();
        DesignerProductDTO product = designerResourceService.getProductDetail(resourceTemplateId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(product, "Fetched product detail"));
    }

    @Operation(
        summary = "Lấy chi tiết một version cụ thể",
        description = "Lấy thông tin chi tiết của một version bao gồm metadata, images, items và thống kê"
    )
    @GetMapping("/versions/{versionId}")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> getVersionDetail(
            @Parameter(description = "ID của version", required = true, example = "1")
            @PathVariable Long versionId) {
        
        Long designerId = getCurrentDesignerId();
        ResourceTemplateVersionDTO version = designerResourceService.getVersionDetail(versionId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(version, "Fetched version detail"));
    }

    @Operation(
        summary = "Tạo version mới cho sản phẩm",
        description = "Tạo version mới với status PENDING_REVIEW. Version mới sẽ được đánh số tự động (v1.0, v2.0, ...). " +
                      "Cần cung cấp đầy đủ thông tin: name, price, releaseDate, images và items."
    )
    @PostMapping("/{resourceTemplateId}/versions")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> createNewVersion(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1")
            @PathVariable Long resourceTemplateId,
            
            @Parameter(description = "Thông tin version mới", required = true)
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

    @Operation(
        summary = "Cập nhật version đang PENDING_REVIEW",
        description = "Chỉ có thể cập nhật version có status PENDING_REVIEW (chưa được approve). " +
                      "Có thể update name, description, price, images, items, releaseDate, expiredTime."
    )
    @PutMapping("/versions/{versionId}")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> updateVersion(
            @Parameter(description = "ID của version cần cập nhật", required = true, example = "1")
            @PathVariable Long versionId,
            
            @Parameter(description = "Thông tin cập nhật", required = true)
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

    @Operation(
        summary = "Archive sản phẩm (ngừng bán)",
        description = "Ngừng bán sản phẩm trên marketplace. Sản phẩm sẽ không hiển thị cho Customer mới, " +
                      "nhưng Customer đã mua vẫn có thể sử dụng. Có thể unarchive để bán lại sau."
    )
    @PostMapping("/{resourceTemplateId}/archive")
    public ResponseEntity<ApiResponse<DesignerProductDTO>> archiveProduct(
            @Parameter(description = "ID của sản phẩm cần archive", required = true, example = "1")
            @PathVariable Long resourceTemplateId) {
        
        Long designerId = getCurrentDesignerId();
        DesignerProductDTO product = designerResourceService.archiveProduct(resourceTemplateId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(product, "Product archived successfully"));
    }

    @Operation(
        summary = "Unarchive sản phẩm (bán lại)",
        description = "Kích hoạt lại sản phẩm đã archive để bán trên marketplace. " +
                      "Sản phẩm sẽ lại hiển thị cho Customer."
    )
    @PostMapping("/{resourceTemplateId}/unarchive")
    public ResponseEntity<ApiResponse<DesignerProductDTO>> unarchiveProduct(
            @Parameter(description = "ID của sản phẩm cần unarchive", required = true, example = "1")
            @PathVariable Long resourceTemplateId) {
        
        Long designerId = getCurrentDesignerId();
        DesignerProductDTO product = designerResourceService.unarchiveProduct(resourceTemplateId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(product, "Product unarchived successfully"));
    }

    @Operation(
        summary = "Resubmit version cho Admin review",
        description = "Dùng khi designer muốn gửi lại version PENDING_REVIEW sau khi fix feedback từ Admin. " +
                      "Cập nhật timestamp để Admin biết version đã được chỉnh sửa."
    )
    @PostMapping("/versions/{versionId}/republish")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> republishVersion(
            @Parameter(description = "ID của version cần resubmit", required = true, example = "1")
            @PathVariable Long versionId) {
        
        Long designerId = getCurrentDesignerId();
        ResourceTemplateVersionDTO version = designerResourceService.republishVersion(versionId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(version, "Version resubmitted for review"));
    }

    @Operation(
        summary = "Publish version lên marketplace",
        description = "Chọn một version đã được Admin approve (status = PUBLISHED) làm version chính thức hiển thị cho Customer. " +
                      "Version này sẽ được set làm currentPublishedVersionId của sản phẩm. " +
                      "Chỉ có thể publish version đã được Admin approve và sản phẩm không bị archive."
    )
    @PostMapping("/versions/{versionId}/publish")
    public ResponseEntity<ApiResponse<DesignerProductDTO>> publishVersion(
            @Parameter(description = "ID của version cần publish", required = true, example = "1")
            @PathVariable Long versionId) {
        
        Long designerId = getCurrentDesignerId();
        DesignerProductDTO product = designerResourceService.publishVersion(versionId, designerId);
        
        return ResponseEntity.ok(ApiResponse.success(product, "Version published successfully"));
    }

    @Operation(
        summary = "Lấy danh sách versions của sản phẩm",
        description = "Lấy tất cả versions của một sản phẩm với phân trang, sắp xếp theo ngày tạo mới nhất. " +
                      "Bao gồm cả version PENDING_REVIEW, PUBLISHED và REJECTED."
    )
    @GetMapping("/{resourceTemplateId}/versions")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateVersionDTO>>> getProductVersions(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1")
            @PathVariable Long resourceTemplateId,
            
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Số lượng items mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Long designerId = getCurrentDesignerId();
        PagedResponseDTO<ResourceTemplateVersionDTO> result = designerResourceService.getProductVersions(
                resourceTemplateId, designerId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched product versions"));
    }

    @Operation(
        summary = "Xóa version",
        description = "Xóa vĩnh viễn một version. Chỉ có thể xóa version có status PENDING_REVIEW. " +
                      "Version đã PUBLISHED hoặc REJECTED không thể xóa."
    )
    @DeleteMapping("/versions/{versionId}")
    public ResponseEntity<ApiResponse<String>> deleteVersion(
            @Parameter(description = "ID của version cần xóa", required = true, example = "1")
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
