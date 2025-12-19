package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.BlogStatus;
import com.sketchnotes.identityservice.model.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findByIdAndDeletedAtIsNull(Long id);
    List<Blog> findByAuthorIdAndDeletedAtIsNull(Long authorId);
    Optional<Blog> findBlogsByIdAndDeletedAtIsNull(Long id);
    Page<Blog> findBlogsByStatusAndDeletedAtIsNull(BlogStatus status, Pageable pageable);
    
    /**
     * Find blogs that need moderation (PENDING_REVIEW status, created before specified time, not deleted)
     */
    @Query("SELECT b FROM Blog b WHERE b.status = :status AND b.deletedAt IS NULL")
    List<Blog> findBlogsForModeration(@Param("status") BlogStatus status);
}