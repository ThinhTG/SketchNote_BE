package com.sketchnotes.identityservice.dto.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshTokenParam {
    String grant_type;
    String client_id;
    String client_secret;
    String refresh_token;
}
