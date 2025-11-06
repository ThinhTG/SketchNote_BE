package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.ListPageRequest;
import com.sketchnotes.project_service.dtos.request.PageRequest;
import com.sketchnotes.project_service.dtos.request.UpdatePageRequest;
import com.sketchnotes.project_service.dtos.response.PageResponse;
import com.sketchnotes.project_service.service.IPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {
    private final IPageService pageService;
//    @PostMapping
//    public ResponseEntity<ApiResponse<PageResponse>> addPage( @RequestBody PageRequest dto) {
//        PageResponse response = pageService.addPage(dto);
//        return ResponseEntity.ok(ApiResponse.success(response, "Page added successfully"));
//    }
    @PostMapping
    public ResponseEntity<ApiResponse<List<PageResponse>>> addPageOfProject( @RequestBody ListPageRequest dto) {
        List<PageResponse> response = pageService.addPages(dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Page added successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PageResponse>>> getPages(@PathVariable Long projectId) {
        List<PageResponse> responses = pageService.getPagesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Pages retrieved successfully"));
    }

    @PutMapping("/{pageId}")
    public ResponseEntity<ApiResponse<PageResponse>> updatePage(@PathVariable Long pageId,
                                                                            @RequestBody UpdatePageRequest dto) {
        PageResponse response = pageService.updatePage(pageId, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Page updated successfully"));
    }

    @DeleteMapping("/{pageId}")
    public ResponseEntity<ApiResponse<String>> deletePage( @PathVariable Long pageId) {
        pageService.deletePage(pageId);
        return ResponseEntity.ok(ApiResponse.success("Page deleted successfully"));
    }
    @DeleteMapping("/test-cicd")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success(" successfully"));
    }
}
