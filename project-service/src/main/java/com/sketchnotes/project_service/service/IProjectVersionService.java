package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.response.ProjectVersionResponse;

import java.util.List;

public interface IProjectVersionService {
    List<ProjectVersionResponse> getRecentVersions(Long projectId);
    void restoreVersion(Long projectId, Long versionId);
}
