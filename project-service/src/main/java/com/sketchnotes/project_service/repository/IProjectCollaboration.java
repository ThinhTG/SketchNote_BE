package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.ProjectCollaboration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProjectCollaboration extends JpaRepository<ProjectCollaboration, Long> {
}
