package com.sketchnotes.identityservice.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class RoleKeycloakRequest {
    private  String id;
    private String name;
}
