package com.sketchnotes.identityservice.dtos.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteActionsEmailParam {
    private List<String> actions;
    private String clientId;
    private Integer lifespan; // in seconds
    private String redirectUri;
}
