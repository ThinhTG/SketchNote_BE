package com.sketchnotes.identityservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "project-service", path = "/api/projects")
public interface ProjectServiceClient {
    
    @GetMapping("/count")
    Integer getProjectCountByOwnerId(@RequestParam("ownerId") Long ownerId);
}
