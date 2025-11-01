package com.sketchnotes.blog_service.Repository;

import com.sketchnotes.blog_service.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
List<Content> findByBlogIdOrderByIndexAsc(Long blogId);
Optional<Content> findByIdAndDeletedAtIsNull(Long id);
}
