package com.sketchnotes.project_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
    name = "ai-service", 
    url = "${ai.service.url:http://34.126.98.83:8000}",
    configuration = AiClientConfig.class
)
public interface AiClient {
    
    /**
     * Remove background from image
     * @param file Image file to process
     * @return Processed image as byte array (PNG format)
     */
    @PostMapping(value = "/bg/remove", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<byte[]> removeBackground(@RequestPart("file") MultipartFile file);
}
