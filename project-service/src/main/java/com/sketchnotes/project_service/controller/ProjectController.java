package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;
import com.sketchnotes.project_service.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final IProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@RequestBody ProjectRequest dto) {
        ProjectResponse response = projectService.createProject(dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Project created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> get(@PathVariable Long id) {
        ProjectResponse response = projectService.getProject(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Get data successful"));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getByOwner(@PathVariable Long ownerId) {
        List<ProjectResponse> response = projectService.getProjectsByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Get data successful"));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getByCurrentUser() {
        List<ProjectResponse> response = projectService.getProjectsCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response, "Get data successful"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(@PathVariable Long id, @RequestBody ProjectRequest dto) {
        ProjectResponse response = projectService.updateProject(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Update successful"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }
}
