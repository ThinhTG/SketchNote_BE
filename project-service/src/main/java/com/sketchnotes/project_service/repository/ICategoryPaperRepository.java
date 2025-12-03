package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.CategoryPaper;
import com.sketchnotes.project_service.enums.PaperType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICategoryPaperRepository extends JpaRepository<CategoryPaper, Long> {
    Page<CategoryPaper> findAllByDeletedAtIsNull(Pageable pageable);
    
    Optional<CategoryPaper> findByCategoryPaperIdAndDeletedAtIsNull(Long id);
    
    @Query("SELECT c FROM CategoryPaper c WHERE " +
            "(:paperType IS NULL OR c.paperType = :paperType) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "c.deletedAt IS NULL")
    Page<CategoryPaper> search(@Param("paperType") PaperType paperType,
                               @Param("keyword") String keyword, 
                               Pageable pageable);

    boolean existsByNameAndDeletedAtIsNull(String name);
}
