package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.client.IUserClient;
import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectListResponse;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;
import com.sketchnotes.project_service.dtos.response.ProjectDetailResponse;
import com.sketchnotes.project_service.service.IProjectService;
import com.sketchnotes.project_service.utils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final IProjectService projectService;
    private final IUserClient userClient;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@RequestBody ProjectRequest dto) {
        ProjectResponse response = projectService.createProject(dto,userClient.getCurrentUser().getResult().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Project created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> get(@PathVariable Long id) {
        ProjectDetailResponse response = projectService.getProject(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Get data successful"));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<ProjectListResponse>> getByOwner(@PathVariable Long ownerId) {
        ProjectListResponse response = projectService.getProjectsByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Get data successful"));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> getByCurrentUser(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PagedResponse<ProjectResponse> response = projectService.getProjectsCurrentUserPaged(
                userClient.getCurrentUser().getResult().getId(), pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response, "Get data successful"));
    }
    @GetMapping("/me/shared")
    public ResponseEntity<ApiResponse<ProjectListResponse>> getShareProjectByCurrentUser() {
        ProjectListResponse response = projectService.getSharedProjectsCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response, "Get data successful"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(@PathVariable Long id, @RequestBody ProjectRequest dto) {
        ProjectResponse response = projectService.updateProject(id, dto,userClient.getCurrentUser().getResult().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Update successful"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        projectService.deleteProject(id,userClient.getCurrentUser().getResult().getId());
        return ResponseEntity.ok(ApiResponse.success(null,"Project deleted successfully"));
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getProjectCount(@RequestParam Long ownerId) {
        Long count = projectService.getProjectCountByOwner(ownerId);
        return ResponseEntity.ok(count.intValue());
    }
}
