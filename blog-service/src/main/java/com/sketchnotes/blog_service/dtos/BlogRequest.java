package com.sketchnotes.blog_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequest {

    private String title;
    private String content;
    private Long authorId; // client cung cấp id người dùng (từ token hoặc frontend)
}