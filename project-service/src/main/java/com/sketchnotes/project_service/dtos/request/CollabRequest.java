package com.sketchnotes.project_service.dtos.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CollabRequest {
    private Long projectId;
    private String email;
    private boolean isEdited;
}
