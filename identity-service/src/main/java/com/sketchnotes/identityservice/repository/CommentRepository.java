package com.sketchnotes.identityservice.repository;


import com.sketchnotes.identityservice.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    // Legacy methods - kept for backward compatibility
    List<Comment> findByBlogId(Long postId);
    List<Comment> findByBlogIdAndParentCommentIdIsNull(Long postId);
    List<Comment> findByParentCommentId(Long parentCommentId);
    
    // Paginated queries for root comments (parent is null)
    Page<Comment> findByBlogIdAndParentCommentIdIsNull(Long blogId, Pageable pageable);
    
    // Paginated queries for replies (by parent comment id)
    Page<Comment> findByParentCommentId(Long parentId, Pageable pageable);
    
    // Count replies for a comment (for reply count)
    Long countByParentCommentId(Long parentId);
    
    // Find non-deleted comment by ID
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
    
    // Custom query to count replies excluding soft-deleted ones
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentCommentId = :parentId AND c.deletedAt IS NULL")
    Long countRepliesByParentId(@Param("parentId") Long parentId);
}

