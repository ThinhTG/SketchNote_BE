package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.ImagePrompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IImagePromptRepository extends JpaRepository<ImagePrompt, Long> {
    Page<ImagePrompt> findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long ownerId, Pageable pageable);
}
