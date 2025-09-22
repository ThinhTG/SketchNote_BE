package com.sketchnotes.userservice.pojo.response;

import lombok.*;

import lombok.AllArgsConstructor;
import lombok.Data;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;
}
