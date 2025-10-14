package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);
    List<Project> findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long ownerId);
}