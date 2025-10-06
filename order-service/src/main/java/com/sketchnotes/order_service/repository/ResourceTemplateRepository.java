package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.entity.ResourceTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceTemplateRepository extends JpaRepository<ResourceTemplate, Long> {
    
    /**
     * Lấy tất cả template đang active
     */
    List<ResourceTemplate> findByIsActiveTrue();
    
    /**
     * Lấy tất cả template đang active với pagination
     */
    Page<ResourceTemplate> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Lấy template theo designer ID và đang active
     */
    List<ResourceTemplate> findByDesignerIdAndIsActiveTrue(Long designerId);
    
    /**
     * Lấy template theo designer ID và đang active với pagination
     */
    Page<ResourceTemplate> findByDesignerIdAndIsActiveTrue(Long designerId, Pageable pageable);
    
    /**
     * Lấy template theo loại và đang active
     */
    List<ResourceTemplate> findByTypeAndIsActiveTrue(ResourceTemplate.TemplateType type);
    
    /**
     * Lấy template theo loại và đang active với pagination
     */
    Page<ResourceTemplate> findByTypeAndIsActiveTrue(ResourceTemplate.TemplateType type, Pageable pageable);
    
    /**
     * Lấy template theo khoảng giá và đang active
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.isActive = true AND rt.price BETWEEN :minPrice AND :maxPrice")
    List<ResourceTemplate> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                           @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    /**
     * Lấy template theo khoảng giá và đang active với pagination
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.isActive = true AND rt.price BETWEEN :minPrice AND :maxPrice")
    Page<ResourceTemplate> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                           @Param("maxPrice") java.math.BigDecimal maxPrice, 
                                           Pageable pageable);
    
    /**
     * Tìm kiếm template theo từ khóa
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.isActive = true AND " +
           "(LOWER(rt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rt.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ResourceTemplate> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Tìm kiếm template theo từ khóa với pagination
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.isActive = true AND " +
           "(LOWER(rt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rt.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ResourceTemplate> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Lấy template theo ID và đang active
     */
    Optional<ResourceTemplate> findByTemplateIdAndIsActiveTrue(Long templateId);
    
    /**
     * Lấy template theo trạng thái active/inactive
     */
    List<ResourceTemplate> findByIsActive(Boolean isActive);
    
    /**
     * Lấy template sắp hết hạn
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.isActive = true AND rt.expiredTime IS NOT NULL AND rt.expiredTime <= :expiryDate")
    List<ResourceTemplate> findByExpiredTimeBefore(@Param("expiryDate") LocalDate expiryDate);
    
    /**
     * Lấy template mới nhất
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.isActive = true ORDER BY rt.createdAt DESC")
    List<ResourceTemplate> findTopByIsActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Đếm số lượng template theo designer
     */
    Long countByDesignerIdAndIsActiveTrue(Long designerId);
    
    /**
     * Đếm số lượng template theo loại
     */
    Long countByTypeAndIsActiveTrue(ResourceTemplate.TemplateType type);
    
    /**
     * Lấy template theo designer ID (không phân biệt trạng thái)
     */
    List<ResourceTemplate> findByDesignerId(Long designerId);
    
    /**
     * Kiểm tra template có tồn tại và active không
     */
    boolean existsByTemplateIdAndIsActiveTrue(Long templateId);
}
