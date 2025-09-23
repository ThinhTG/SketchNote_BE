package com.sketchnotes.userservice.pojo.request;

import com.sketchnotes.userservice.enums.Role;
import lombok.*;

import lombok.Data;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private Role role;
    private String avatarUrl;
}
