package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.ProjectCollaboration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProjectCollaborationRepository extends JpaRepository<ProjectCollaboration, Long> {
}
