package com.sketchnotes.identityservice.dto.identity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleLoginParam {
    String grant_type;
    String client_id;
    String client_secret;
    String code;
    String redirect_uri;
}
