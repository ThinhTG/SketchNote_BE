package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.enums.FileContentType;
import com.sketchnotes.project_service.service.IStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class StorageController {

    private final IStorageService storageService;

    @GetMapping("/storage/presign")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPresignedUrl(
                @RequestParam String fileName,
                        @RequestParam FileContentType contentType) {
        Map<String, String> response = storageService.generatePresignedUrl(fileName, contentType);
        return ResponseEntity.ok(ApiResponse.success(response, "Presigned URL generated successfully"));
    }

    @PostMapping("/storage/copy")
    public ResponseEntity<ApiResponse<Map<String, String>>> copyFile(
                @RequestParam String sourceFileUrl) {
        String newFileUrl = storageService.copyFile(sourceFileUrl);
        Map<String, String> response = Map.of("newFileUrl", newFileUrl);
        return ResponseEntity.ok(ApiResponse.success(response, "File copied successfully"));
    }
}