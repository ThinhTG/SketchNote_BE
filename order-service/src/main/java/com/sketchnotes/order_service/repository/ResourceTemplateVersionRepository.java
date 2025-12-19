package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.entity.ResourceTemplate;
import com.sketchnotes.order_service.entity.ResourceTemplateVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceTemplateVersionRepository extends JpaRepository<ResourceTemplateVersion, Long> {
    /**
     * Lấy tất cả versions của một template
     */
    List<ResourceTemplateVersion> findByTemplateIdOrderByCreatedAtDesc(Long templateId);

    /**
     * Lấy tất cả versions của một template với pagination
     */
    Page<ResourceTemplateVersion> findByTemplateIdOrderByCreatedAtDesc(Long templateId, Pageable pageable);

    /**
     * Lấy versions theo trạng thái
     */
    List<ResourceTemplateVersion> findByTemplateIdAndStatusOrderByCreatedAtDesc(
            Long templateId, 
            ResourceTemplate.TemplateStatus status
    );

    /**
     * Lấy versions theo trạng thái với pagination
     */
    Page<ResourceTemplateVersion> findByTemplateIdAndStatusOrderByCreatedAtDesc(
            Long templateId, 
            ResourceTemplate.TemplateStatus status,
            Pageable pageable
    );

    /**
     * Lấy version được publish hiện tại của template
     */
    Optional<ResourceTemplateVersion> findByTemplateIdAndStatus(
            Long templateId,
            ResourceTemplate.TemplateStatus status
    );

    /**
     * Lấy version mới nhất của một template (bất kể status)
     */
    @Query("SELECT rtv FROM ResourceTemplateVersion rtv WHERE rtv.templateId = :templateId " +
           "ORDER BY rtv.createdAt DESC LIMIT 1")
    Optional<ResourceTemplateVersion> findLatestByTemplateId(@Param("templateId") Long templateId);

    /**
     * Kiểm tra version number đã tồn tại chưa
     */
    boolean existsByTemplateIdAndVersionNumber(Long templateId, String versionNumber);

    /**
     * Tính tiếp theo version number (e.g., 1.0 -> 2.0)
     */
    @Query("SELECT rtv FROM ResourceTemplateVersion rtv WHERE rtv.templateId = :templateId " +
           "ORDER BY rtv.createdAt DESC LIMIT 1")
    Optional<ResourceTemplateVersion> findLastVersionByTemplateId(@Param("templateId") Long templateId);

    /**
     * Lấy tất cả PUBLISHED versions
     */
    List<ResourceTemplateVersion> findByStatusOrderByCreatedAtDesc(ResourceTemplate.TemplateStatus status);

    /**
     * Lấy PENDING_REVIEW versions
     */
    Page<ResourceTemplateVersion> findByStatusOrderByCreatedAtDesc(
            ResourceTemplate.TemplateStatus status,
            Pageable pageable
    );

    /**
     * Lấy versions được tạo bởi designer
     */
    List<ResourceTemplateVersion> findByCreatedByOrderByCreatedAtDesc(Long designerId);

    /**
     * Lấy versions được tạo bởi designer với pagination
     */
    Page<ResourceTemplateVersion> findByCreatedByOrderByCreatedAtDesc(Long designerId, Pageable pageable);

    /**
     * Lấy versions được tạo bởi designer với status filter
     */
    Page<ResourceTemplateVersion> findByCreatedByAndStatusOrderByCreatedAtDesc(
            Long designerId,
            ResourceTemplate.TemplateStatus status,
            Pageable pageable
    );

    /**
     * Lấy tất cả PUBLISHED versions của template (từ cũ đến mới)
     */
    @Query("SELECT v FROM ResourceTemplateVersion v WHERE v.templateId = :templateId " +
           "AND v.status = 'PUBLISHED' ORDER BY v.createdAt ASC")
    List<ResourceTemplateVersion> findPublishedVersionsByTemplateIdOrderByCreatedAtAsc(
            @Param("templateId") Long templateId
    );

    /**
     * Lấy tất cả PUBLISHED versions từ một version ID trở đi (bao gồm version đó)
     * Used to get all versions user has access to (purchased version + newer versions)
     */
    @Query("SELECT v FROM ResourceTemplateVersion v WHERE v.templateId = :templateId " +
           "AND v.status = 'PUBLISHED' " +
           "AND v.createdAt >= (SELECT v2.createdAt FROM ResourceTemplateVersion v2 WHERE v2.versionId = :fromVersionId) " +
           "ORDER BY v.createdAt ASC")
    List<ResourceTemplateVersion> findPublishedVersionsFromVersionId(
            @Param("templateId") Long templateId,
            @Param("fromVersionId") Long fromVersionId
    );

    /**
     * Lấy tất cả PUBLISHED versions của một template theo danh sách template IDs
     */
    @Query("SELECT v FROM ResourceTemplateVersion v WHERE v.templateId IN :templateIds " +
           "AND v.status = 'PUBLISHED' ORDER BY v.templateId, v.createdAt ASC")
    List<ResourceTemplateVersion> findPublishedVersionsByTemplateIds(
            @Param("templateIds") List<Long> templateIds
    );
}
