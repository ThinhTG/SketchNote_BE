package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.ProjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProjectVersion extends JpaRepository<ProjectVersion, Long> {
}
