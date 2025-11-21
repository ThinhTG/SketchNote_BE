package com.sketchnotes.project_service.client;

import com.sketchnotes.project_service.dtos.response.UserQuotaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-service", path = "/api/users")
public interface IdentityServiceClient {
    
    @GetMapping("/{userId}/quota")
    UserQuotaResponse getUserQuota(@PathVariable("userId") Long userId);
}
