package com.sketchnotes.identityservice.dto.request;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class RoleRequest {
    private String roleId;
    private  Long userId;
}
