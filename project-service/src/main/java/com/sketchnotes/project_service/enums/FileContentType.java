package com.sketchnotes.project_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileContentType {
    JSON("application/json"),
    TEXT("text/plain"),
    PDF("application/pdf"),
    VIDEO_MP4("video/mp4"),
    IMAGE_PNG("image/png"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_SVG("image/svg+xml");

    private final String mimeType;
}
