package com.sketchnotes.identityservice.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class LoginResponse {
    private String token;
}
