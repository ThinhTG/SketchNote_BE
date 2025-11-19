package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.entity.ProjectCollaboration;
import com.sketchnotes.project_service.entity.ProjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProjectCollaborationRepository extends JpaRepository<ProjectCollaboration, Long> {
    List<ProjectCollaboration> findByProjectAndDeletedAtIsNull(Project project);
    Optional<ProjectCollaboration> findByProjectAndUserIdAndDeletedAtIsNull(Project project, Long userId);
    List<ProjectCollaboration> findByUserIdAndDeletedAtIsNull(Long userId);
}
