package com.sketchnotes.identityservice.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class LoginGoogleRequest {
    private String code;
    private  String redirectUri;
}
