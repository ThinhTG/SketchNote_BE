package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.ProjectDTO;

import java.util.List;

public interface ProjectService {
    ProjectDTO createProject(ProjectDTO dto);
    ProjectDTO getProject(Long id);
    List<ProjectDTO> getProjectsByOwner(Long ownerId);
    ProjectDTO updateProject(Long id, ProjectDTO dto);
    void deleteProject(Long id);
}

