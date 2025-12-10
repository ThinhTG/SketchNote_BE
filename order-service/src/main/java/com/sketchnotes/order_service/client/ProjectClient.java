package com.sketchnotes.order_service.client;


import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.FileRequest;
import com.sketchnotes.order_service.dtos.project.ProjectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "project-service"
)
public interface ProjectClient {
    @GetMapping("/api/projects/{id}")
    ApiResponse<ProjectResponse> getProject(@PathVariable Long id);
    
    @PostMapping("/api/projects/storage/copy")
    ApiResponse<Map<String, String>> copyFile(@RequestBody FileRequest fileRequest);
}
