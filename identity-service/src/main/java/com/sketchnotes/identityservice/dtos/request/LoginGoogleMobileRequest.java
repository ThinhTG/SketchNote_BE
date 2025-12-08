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
public class LoginGoogleMobileRequest {
    @NotBlank(message = "ID Token is required")
    private String idToken;
}
