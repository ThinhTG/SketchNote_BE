package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.BlogModerationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogModerationHistoryRepository extends JpaRepository<BlogModerationHistory, Long> {
    
    /**
     * Find all moderation history for a specific blog, ordered by most recent first
     */
    List<BlogModerationHistory> findByBlogIdOrderByCheckedAtDesc(Long blogId);
    
    /**
     * Find the latest moderation record for a blog
     */
    @Query("SELECT h FROM BlogModerationHistory h WHERE h.blog.id = :blogId ORDER BY h.checkedAt DESC LIMIT 1")
    Optional<BlogModerationHistory> findLatestByBlogId(@Param("blogId") Long blogId);
    
    /**
     * Count total moderation checks for a blog
     */
    long countByBlogId(Long blogId);
}
