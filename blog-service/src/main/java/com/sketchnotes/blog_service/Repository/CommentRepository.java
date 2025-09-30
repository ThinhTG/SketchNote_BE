package com.sketchnotes.blog_service.Repository;

import com.sketchnotes.blog_service.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByBlogId(Long postId);
    List<Comment> findByBlogIdAndParentCommentIdIsNull(Long postId);
    List<Comment> findByParentCommentId(Long parentCommentId);
}
