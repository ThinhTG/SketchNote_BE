package com.sketchnotes.identityservice.dtos.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequest {
    private String sectionTitle;
    private String content;
    private String contentUrl;
    private Integer index;
}
