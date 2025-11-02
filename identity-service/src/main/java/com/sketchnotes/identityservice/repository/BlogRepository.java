package com.sketchnotes.identityservice.repository;


import com.sketchnotes.identityservice.model.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findByIdAndDeletedAtIsNull(Long id);
    List<Blog> findByAuthorIdAndDeletedAtIsNull(Long authorId);
    Optional<Blog> findBlogsByIdAndDeletedAtIsNull(Long id);
    Page<Blog> findBlogsByDeletedAtIsNull(Pageable pageable);
}