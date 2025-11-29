package com.sketchnotes.project_service.enums;

import lombok.Getter;

@Getter
public enum ImageType {
    PNG(".png", "image/png"),
    JPEG(".jpeg", "image/jpeg"),
    JPG(".jpg", "image/jpeg"),
    WEBP(".webp", "image/webp");

    private final String extension;
    private final String contentType;

    ImageType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }
}