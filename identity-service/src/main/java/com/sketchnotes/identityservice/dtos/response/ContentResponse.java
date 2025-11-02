package com.sketchnotes.identityservice.dtos.response;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentResponse {
    private Long id;
    private String sectionTitle;
    private String content;
    private String contentUrl;
    private Integer index;
}
