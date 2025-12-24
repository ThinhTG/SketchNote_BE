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
     * Lấy tất cả template theo status
     */
    List<ResourceTemplate> findByStatus(ResourceTemplate.TemplateStatus status);

    /**
     * Lấy tất cả template active (PUBLISHED status) cho Customer queries
     * State Machine: Chỉ lấy status = PUBLISHED (không bao gồm ARCHIVED, REJECTED, DELETED)
     */
    Page<ResourceTemplate> findByStatus(ResourceTemplate.TemplateStatus status, Pageable pageable);

    /**
     * Lấy template theo designer ID và status
     */
    List<ResourceTemplate> findByDesignerIdAndStatus(Long designerId, ResourceTemplate.TemplateStatus status);

    /**
     * Lấy template theo designer ID và status với pagination
     */
    Page<ResourceTemplate> findByDesignerIdAndStatus(Long designerId, ResourceTemplate.TemplateStatus status, Pageable pageable);

    /**
     * Lấy tất cả template của designer với pagination
     */
    Page<ResourceTemplate> findByDesignerId(Long designerId, Pageable pageable);

    /**
     * Lấy template của designer theo status với pagination
     * Thay thế isArchived bằng status filter (ARCHIVED, PUBLISHED, etc.)
     */
    Page<ResourceTemplate> findByDesignerIdAndStatusIn(Long designerId, List<ResourceTemplate.TemplateStatus> statuses, Pageable pageable);

    /**
     * Tìm kiếm template của designer theo keyword (tên hoặc mô tả) với pagination
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.designerId = :designerId AND " +
           "(LOWER(rt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rt.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ResourceTemplate> searchByDesignerIdAndKeyword(
            @Param("designerId") Long designerId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * Tìm kiếm template của designer theo keyword và status với pagination
     * Thay thế isArchived bằng status filter
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.designerId = :designerId AND rt.status = :status AND " +
           "(LOWER(rt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rt.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ResourceTemplate> searchByDesignerIdAndKeywordAndStatus(
            @Param("designerId") Long designerId,
            @Param("keyword") String keyword,
            @Param("status") ResourceTemplate.TemplateStatus status,
            Pageable pageable);

    /**
     * Lấy template theo loại và status
     */
    List<ResourceTemplate> findByTypeAndStatus(ResourceTemplate.TemplateType type, ResourceTemplate.TemplateStatus status);

    /**
     * Lấy template theo loại và status với pagination
     * State Machine: Customer chỉ thấy status = PUBLISHED
     */
    Page<ResourceTemplate> findByTypeAndStatus(ResourceTemplate.TemplateType type, ResourceTemplate.TemplateStatus status, Pageable pageable);

    /**
     * Lấy template theo khoảng giá và status
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.status = :status AND rt.price BETWEEN :minPrice AND :maxPrice")
    List<ResourceTemplate> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice,
                                           @Param("maxPrice") java.math.BigDecimal maxPrice,
                                           @Param("status") ResourceTemplate.TemplateStatus status);

    /**
     * Lấy template theo khoảng giá và status với pagination
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.status = :status AND rt.price BETWEEN :minPrice AND :maxPrice")
    Page<ResourceTemplate> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice,
                                           @Param("maxPrice") java.math.BigDecimal maxPrice,
                                           @Param("status") ResourceTemplate.TemplateStatus status,
                                           Pageable pageable);

    /**
     * Tìm kiếm template theo từ khóa và status
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.status = :status AND " +
           "(LOWER(rt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rt.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ResourceTemplate> searchByKeyword(@Param("keyword") String keyword, @Param("status") ResourceTemplate.TemplateStatus status);

    /**
     * Tìm kiếm template theo từ khóa với pagination và status
     * State Machine: Customer queries chỉ dùng status = PUBLISHED
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.status = :status AND " +
           "(LOWER(rt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rt.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ResourceTemplate> searchByKeyword(@Param("keyword") String keyword, @Param("status") ResourceTemplate.TemplateStatus status, Pageable pageable);

    /**
     * Lấy template theo ID và status
     * State Machine: Customer queries chỉ dùng status = PUBLISHED
     */
    Optional<ResourceTemplate> findByTemplateIdAndStatus(Long templateId, ResourceTemplate.TemplateStatus status);

    /**
     * Lấy danh sách template theo nhiều IDs và status
     */
    List<ResourceTemplate> findByTemplateIdInAndStatus(List<Long> templateIds, ResourceTemplate.TemplateStatus status);

    /**
     * Lấy template mới nhất theo status
     */
    @Query("SELECT rt FROM ResourceTemplate rt WHERE rt.status = :status ORDER BY rt.createdAt DESC")
    List<ResourceTemplate> findTopByStatusOrderByCreatedAtDesc(@Param("status") ResourceTemplate.TemplateStatus status);

    /**
     * Kiểm tra template có tồn tại và có status cụ thể không
     */
    boolean existsByTemplateIdAndStatus(Long templateId, ResourceTemplate.TemplateStatus status);

    /**
     * Đếm tổng số template của designer
     */
    long countByDesignerId(Long designerId);

}
