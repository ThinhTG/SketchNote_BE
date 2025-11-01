package com.sketchnotes.blog_service.Repository;

import com.sketchnotes.blog_service.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findByIdAndDeletedAtIsNull(Long id);
    List<Blog> findByAuthorId(Long authorId);
    Optional<Blog> findBlogsByIdAndDeletedAtIsNull(Long id);
    Page<Blog> findBlogsByDeletedAtIsNull(Pageable pageable);
}