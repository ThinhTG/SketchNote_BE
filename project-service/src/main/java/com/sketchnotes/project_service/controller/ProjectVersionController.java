package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.response.ProjectVersionResponse;
import com.sketchnotes.project_service.service.IProjectVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/versions")
@RequiredArgsConstructor
public class ProjectVersionController {
    private final IProjectVersionService projectVersionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectVersionResponse>>> getRecentVersions(
            @PathVariable Long projectId) {
        List<ProjectVersionResponse> versions = projectVersionService.getRecentVersions(projectId);
        return ResponseEntity.ok(ApiResponse.success(versions, "Get versions successful"));
    }

    @PostMapping("/{versionId}/restore")
    public ResponseEntity<ApiResponse<String>> restoreVersion(
            @PathVariable Long projectId,
            @PathVariable Long versionId) {
        projectVersionService.restoreVersion(projectId, versionId);
        return ResponseEntity.ok(ApiResponse.success("Version restored successfully"));
    }
}
