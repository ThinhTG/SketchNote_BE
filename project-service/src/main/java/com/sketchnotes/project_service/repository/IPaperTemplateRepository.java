package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.PaperTemplate;
import com.sketchnotes.project_service.enums.PaperSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPaperTemplateRepository extends JpaRepository<PaperTemplate, Long> {
    Page<PaperTemplate> findAllByDeletedAtIsNull(Pageable pageable);
    
    Optional<PaperTemplate> findByPaperTemplateIdAndDeletedAtIsNull(Long id);
    
    @Query("SELECT p FROM PaperTemplate p WHERE " +
            "(:categoryId IS NULL OR p.categoryPaper.categoryPaperId = :categoryId) AND " +
            "(:paperSize IS NULL OR p.paperSize = :paperSize) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "p.deletedAt IS NULL")
    Page<PaperTemplate> search(@Param("categoryId") Long categoryId,
                               @Param("paperSize") PaperSize paperSize,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    boolean existsByNameAndDeletedAtIsNull(String name);
}
