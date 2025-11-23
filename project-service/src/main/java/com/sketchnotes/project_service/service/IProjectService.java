package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectListResponse;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;

import java.util.List;

import com.sketchnotes.project_service.dtos.response.ProjectDetailResponse;

public interface IProjectService {
    ProjectResponse createProject(ProjectRequest dto, Long ownerId);
    ProjectDetailResponse getProject(Long id);
    ProjectListResponse getProjectsByOwner(Long ownerId);
    ProjectListResponse getProjectsCurrentUser(Long ownerId);
    ProjectResponse updateProject(Long id, ProjectRequest dto, Long ownerId);
    ProjectListResponse getSharedProjectsCurrentUser();
    void deleteProject(Long id, Long ownerId);
    Long getProjectCountByOwner(Long ownerId);
}

