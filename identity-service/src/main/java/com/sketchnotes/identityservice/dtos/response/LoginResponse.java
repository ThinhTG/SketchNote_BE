package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private  String refreshToken;
}
