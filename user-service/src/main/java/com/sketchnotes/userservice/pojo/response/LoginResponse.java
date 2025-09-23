package com.sketchnotes.userservice.pojo.response;

import lombok.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class LoginResponse {
    private String token;
}
