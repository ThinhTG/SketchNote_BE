package com.sketchnotes.identityservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class LoginGoogleRequest {
    @NotBlank(message = "Authorization code is required")
    private String code;
    
    @NotBlank(message = "Redirect URI is required")
    private String redirectUri;
}
