package com.sketchnotes.blog_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserClient {
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    // đơn giản DTO
    static record UserDto(Long id, String username, String fullName) {}
}

