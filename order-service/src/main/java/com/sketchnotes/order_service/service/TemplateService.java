package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.PagedResponseDTO;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.dtos.TemplateCreateUpdateDTO;
import com.sketchnotes.order_service.dtos.TemplateSellDTO;
import com.sketchnotes.order_service.entity.ResourceTemplate;

import java.math.BigDecimal;
import java.util.List;

public interface TemplateService {
    
    /**
     * Lấy tất cả template đang active
     */
    List<ResourceTemplateDTO> getAllActiveTemplates();
    
    /**
     * Lấy tất cả template đang active với pagination
     */
    PagedResponseDTO<ResourceTemplateDTO> getAllActiveTemplates(int page, int size, String sortBy, String sortDir);
    
    /**
     * Lấy template theo ID
     */
    ResourceTemplateDTO getTemplateById(Long id);
    
    /**
     * Lấy template theo designer ID
     */
    List<ResourceTemplateDTO> getTemplatesByDesigner(Long designerId);
    
    /**
     * Lấy template theo designer ID với pagination
     */
    PagedResponseDTO<ResourceTemplateDTO> getTemplatesByDesigner(Long designerId, int page, int size, String sortBy, String sortDir);

    /**
     * Lấy template theo designer ID và status với pagination
     */
    PagedResponseDTO<ResourceTemplateDTO> getTemplatesByDesignerAndStatus(Long designerId, String status, int page, int size, String sortBy, String sortDir);
    
    /**
     * Lấy template theo loại
     */
    List<ResourceTemplateDTO> getTemplatesByType(String type);
    
    /**
     * Lấy template theo loại với pagination
     */
    PagedResponseDTO<ResourceTemplateDTO> getTemplatesByType(String type, int page, int size, String sortBy, String sortDir);
    
    /**
     * Tìm kiếm template theo từ khóa
     */
    List<ResourceTemplateDTO> searchTemplates(String keyword);
    
    /**
     * Tìm kiếm template theo từ khóa với pagination
     */
    PagedResponseDTO<ResourceTemplateDTO> searchTemplates(String keyword, int page, int size, String sortBy, String sortDir);
    
    /**
     * Lấy template theo khoảng giá
     */
    List<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Lấy template theo khoảng giá với pagination
     */
    PagedResponseDTO<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir);
    
    /**
     * Tạo template mới
     */
    ResourceTemplateDTO createTemplate(TemplateCreateUpdateDTO templateDTO);
    
    /**
     * Cập nhật template
     */
    ResourceTemplateDTO updateTemplate(Long id, TemplateCreateUpdateDTO templateDTO);
    
    /**
     * Xóa template (soft delete)
     */
    void deleteTemplate(Long id);
    
    /**
     * Kích hoạt/vô hiệu hóa template
     */
    ResourceTemplateDTO toggleTemplateStatus(Long id);
    
    /**
     * Lấy template theo trạng thái active/inactive
     */
    List<ResourceTemplateDTO> getTemplatesByStatus(Boolean isActive);
    
    /**
     * Lấy template theo trạng thái review (PENDING_REVIEW, PUBLISHED, REJECTED)
     */
    PagedResponseDTO<ResourceTemplateDTO> getTemplatesByReviewStatus(String status, int page, int size, String sortBy, String sortDir);
    
    /**
     * Lấy template sắp hết hạn
     */
    List<ResourceTemplateDTO> getTemplatesExpiringSoon(int days);
    
    /**
     * Lấy template mới nhất
     */
    List<ResourceTemplateDTO> getLatestTemplates(int limit);
    
    /**
     * Lấy template phổ biến nhất (có thể dựa trên số lượng order)
     */
    List<ResourceTemplateDTO> getPopularTemplates(int limit);

    /**
     * Xác nhận template và chuyển trạng thái từ PENDING_REVIEW sang PUBLISHED
     * Chỉ staff mới có quyền thực hiện chức năng này
     */
    ResourceTemplateDTO confirmTemplate(Long id);

    /**
     * Từ chối template và chuyển trạng thái sang REJECTED
     * Chỉ staff mới có quyền thực hiện chức năng này
     */
    ResourceTemplateDTO rejectTemplate(Long id);


    ResourceTemplateDTO createTemplateFromProject(Long projectId, Long userId, TemplateSellDTO templateDTO);
}
