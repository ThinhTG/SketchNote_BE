package com.sketchnotes.identityservice.dtos.identity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginParam {

    String grant_type;
    String client_id;
    String client_secret;
    String scope;
    String username;
    String password;
}
