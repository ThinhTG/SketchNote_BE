package com.sketchnotes.identityservice.repository;


import com.sketchnotes.identityservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByBlogId(Long postId);
    List<Comment> findByBlogIdAndParentCommentIdIsNull(Long postId);
    List<Comment> findByParentCommentId(Long parentCommentId);
}
