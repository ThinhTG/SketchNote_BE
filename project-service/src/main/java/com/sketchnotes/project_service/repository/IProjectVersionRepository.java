package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.entity.ProjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProjectVersionRepository extends JpaRepository<ProjectVersion, Long> {
    ProjectVersion findFirstByProjectAndDeletedAtIsNullOrderByVersionNumberDesc(Project project);
    List<ProjectVersion> findByProjectAndDeletedAtIsNullOrderByCreatedAtDesc(Project project);
    List<ProjectVersion> findByProject_ProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long projectId);
    Long countByProjectAndDeletedAtIsNull(Project project);
    ProjectVersion findFirstByProjectAndDeletedAtIsNullOrderByCreatedAtAsc(Project project);
}
