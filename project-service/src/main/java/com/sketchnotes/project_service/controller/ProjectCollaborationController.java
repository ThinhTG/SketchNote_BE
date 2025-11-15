package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.CollabRequest;
import com.sketchnotes.project_service.dtos.response.ProjectCollaborationResponse;
import com.sketchnotes.project_service.service.IProjectCollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;

@RestController
@RequestMapping("/api/collaborations")
@RequiredArgsConstructor
public class ProjectCollaborationController {
    private final IProjectCollaborationService projectCollaborationService;

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<String>> inviteUser(@RequestBody CollabRequest dto) {
        projectCollaborationService.inviteUserToProject(dto);
        return ResponseEntity.ok(ApiResponse.success("Invite successful"));
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    public ResponseEntity<ApiResponse<String>> removeUser(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectCollaborationService.removeUserFromProject(projectId, userId);
        return ResponseEntity.ok(ApiResponse.success("Removed collaborator successfully"));
    }

    @PutMapping("/permission")
    public ResponseEntity<ApiResponse<String>> changePermission(@RequestBody CollabRequest dto) {
        projectCollaborationService.changeUserPermission(dto);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully"));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<List<ProjectCollaborationResponse>>> listCollaborators(
            @PathVariable Long projectId) {
        List<ProjectCollaborationResponse> collaborators = projectCollaborationService.listProjectCollaborators(projectId);
        return ResponseEntity.ok(ApiResponse.success(collaborators, "Get data successful"));
    }
}
