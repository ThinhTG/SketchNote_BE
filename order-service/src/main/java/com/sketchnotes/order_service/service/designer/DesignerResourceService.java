package com.sketchnotes.order_service.service.designer;

import com.sketchnotes.order_service.dtos.PagedResponseDTO;
import com.sketchnotes.order_service.dtos.designer.CreateResourceVersionDTO;
import com.sketchnotes.order_service.dtos.designer.DesignerProductDTO;
import com.sketchnotes.order_service.dtos.designer.ResourceTemplateVersionDTO;
import com.sketchnotes.order_service.entity.ResourceTemplate;

import java.util.List;

public interface DesignerResourceService {
    /**
     * Lấy danh sách sản phẩm của designer (có phân trang, tìm kiếm, filter)
     * @param search Tìm kiếm theo tên hoặc mô tả (optional)
     * @param statusFilter Filter theo status: ARCHIVED, PUBLISHED, PENDING_REVIEW, etc. (optional)
     */
    PagedResponseDTO<DesignerProductDTO> getMyProducts(Long designerId, int page, int size, String sortBy, String sortDir, String search, ResourceTemplate.TemplateStatus statusFilter);

    /**
     * Lấy chi tiết sản phẩm (bao gồm tất cả versions)
     */
    DesignerProductDTO getProductDetail(Long resourceTemplateId, Long designerId);

    /**
     * Lấy chi tiết một version cụ thể
     */
    ResourceTemplateVersionDTO getVersionDetail(Long versionId, Long designerId);

    /**
     * Tạo version mới cho sản phẩm
     * Có thể upload file hoặc select project từ platform
     */
    ResourceTemplateVersionDTO createNewVersion(Long resourceTemplateId, Long designerId, CreateResourceVersionDTO dto);

    /**
     * Cập nhật metadata của version đang PENDING_REVIEW
     */
    ResourceTemplateVersionDTO updateVersion(Long versionId, Long designerId, CreateResourceVersionDTO dto);

    /**
     * Archive sản phẩm (ngừng bán)
     * Sản phẩm sẽ không hiển thị cho customer mới, nhưng customer cũ vẫn có thể dùng
     */
    DesignerProductDTO archiveProduct(Long resourceTemplateId, Long designerId);

    /**
     * Unarchive sản phẩm
     */
    DesignerProductDTO unarchiveProduct(Long resourceTemplateId, Long designerId);

    /**
     * Republish sản phẩm (từ PENDING_REVIEW -> PUBLISHED)
     * Được sử dụng khi designer muốn resubmit sau khi fix feedback
     */
    ResourceTemplateVersionDTO republishVersion(Long versionId, Long designerId);

    /**
     * Publish một version đã được approve làm version chính thức
     * Designer chọn version nào sẽ hiển thị cho customer
     */
    DesignerProductDTO publishVersion(Long versionId, Long designerId);

    /**
     * Lấy danh sách versions của một sản phẩm
     */
    PagedResponseDTO<ResourceTemplateVersionDTO> getProductVersions(
            Long resourceTemplateId,
            Long designerId,
            int page,
            int size
    );

    /**
     * Xóa version (chỉ xóa được version PENDING_REVIEW)
     */
    void deleteVersion(Long versionId, Long designerId);
}
