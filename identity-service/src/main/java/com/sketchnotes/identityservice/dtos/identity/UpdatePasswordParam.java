package com.sketchnotes.identityservice.dtos.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordParam {
    private String type;
    private String value;
    private boolean temporary;
}
