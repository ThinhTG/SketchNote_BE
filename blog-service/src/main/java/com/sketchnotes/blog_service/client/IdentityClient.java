package com.sketchnotes.blog_service.client;

import com.sketchnotes.blog_service.dtos.response.ApiResponse;
import com.sketchnotes.blog_service.dtos.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient( name = "identity-service")
public interface IdentityClient {

    @GetMapping("/me")
    ApiResponse<UserResponse> getCurrentUser();
    @GetMapping("/api/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);
}
