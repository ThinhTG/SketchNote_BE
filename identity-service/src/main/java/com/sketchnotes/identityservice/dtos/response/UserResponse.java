package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class UserResponse  implements Serializable {
    private Long id;
    private  String keycloakId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String avatarUrl;
}
