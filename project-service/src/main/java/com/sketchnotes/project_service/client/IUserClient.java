package com.sketchnotes.project_service.client;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.CreateNotificationRequest;
import com.sketchnotes.project_service.dtos.response.NotificationDto;
import com.sketchnotes.project_service.dtos.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service")
public interface IUserClient {
    @GetMapping("/api/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);
    @GetMapping("/api/users/keycloak/{sub}")
    ApiResponse<UserResponse> getUserByKeycloakId(@PathVariable("sub") Long id);
    @GetMapping("/api/users/me")
    ApiResponse<UserResponse> getCurrentUser();
    
    @PostMapping("/internal/notifications")
    ResponseEntity<NotificationDto> createNotification(@RequestBody CreateNotificationRequest request);

}
