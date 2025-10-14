package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.enums.FileContentType;

import java.util.Map;

public interface IStorageService {
    Map<String, String> generatePresignedUrl(String fileName, FileContentType contentType);

}
