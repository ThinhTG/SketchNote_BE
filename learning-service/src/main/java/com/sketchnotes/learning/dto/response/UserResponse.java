package com.sketchnotes.learning.dto.response;


import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String keycloakId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String avatarUrl;
}
