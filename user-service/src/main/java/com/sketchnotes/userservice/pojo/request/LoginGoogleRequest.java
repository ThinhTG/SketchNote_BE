package com.sketchnotes.userservice.pojo.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class LoginGoogleRequest {
    private String idToken;
}
