package com.sketchnotes.blog_service.client;

import com.sketchnotes.blog_service.dtos.ApiResponse;
import com.sketchnotes.blog_service.dtos.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "identity-service",
        url = "http://146.190.90.222:8089/api/users"
)
public interface IdentityClient {

    @GetMapping("/me")
    ApiResponse<UserResponse> getCurrentUser();

}
