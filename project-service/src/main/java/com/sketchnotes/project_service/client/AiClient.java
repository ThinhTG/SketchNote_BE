package com.sketchnotes.project_service.client;


import com.sketchnotes.project_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "ai-client",
        url = "${ai.url}",
        configuration = FeignConfig.class
)
public interface AiClient {
    @PostMapping(value = "/bg/remove", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<byte[]> removeBackground(@RequestPart("file") MultipartFile file);
}