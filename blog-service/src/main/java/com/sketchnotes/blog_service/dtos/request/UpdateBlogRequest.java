package com.sketchnotes.blog_service.dtos.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBlogRequest {
    private String title;
    private String summary;
    private String imageUrl;
}
