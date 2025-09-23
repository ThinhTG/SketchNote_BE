package com.sketchnotes.userservice.pojo.request;

import com.sketchnotes.userservice.enums.Role;
import lombok.*;

import lombok.AllArgsConstructor;
import lombok.Data;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UserRequest {

    private String fullName;
    private String avatarUrl;
}
