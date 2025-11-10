package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;

import java.util.List;

public interface IProjectService {
    ProjectResponse createProject(ProjectRequest dto, Long ownerId);
    ProjectResponse getProject(Long id);
    List<ProjectResponse> getProjectsByOwner(Long ownerId);
    List<ProjectResponse> getProjectsCurrentUser(Long ownerId);
    ProjectResponse updateProject(Long id, ProjectRequest dto, Long ownerId);
    void deleteProject(Long id, Long ownerId);
}

