package com.sketchnotes.identityservice.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequest {

    private String title;
    private String summary;
    private String imageUrl;
    private List<ContentRequest> contents;
}