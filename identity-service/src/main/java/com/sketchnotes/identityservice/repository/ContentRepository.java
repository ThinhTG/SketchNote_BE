package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByBlogIdAndDeletedAtIsNullOrderByIndexAsc(Long blogId);
Optional<Content> findByIdAndDeletedAtIsNull(Long id);
}
