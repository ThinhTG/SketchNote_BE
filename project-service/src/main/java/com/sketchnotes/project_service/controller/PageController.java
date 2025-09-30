package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.PageDTO;
import com.sketchnotes.project_service.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/pages")
@RequiredArgsConstructor
public class PageController {
    private final PageService pageService;

    @PostMapping
    public ResponseEntity<PageDTO> addPage(@PathVariable Long projectId, @RequestBody PageDTO dto) {
        return ResponseEntity.ok(pageService.addPage(projectId, dto));
    }

    @GetMapping
    public ResponseEntity<List<PageDTO>> getPages(@PathVariable Long projectId) {
        return ResponseEntity.ok(pageService.getPagesByProject(projectId));
    }

    @PutMapping("/{pageId}")
    public ResponseEntity<PageDTO> updatePage(@PathVariable Long projectId,
                                              @PathVariable Long pageId,
                                              @RequestBody PageDTO dto) {
        return ResponseEntity.ok(pageService.updatePage(pageId, dto));
    }

    @DeleteMapping("/{pageId}")
    public ResponseEntity<Void> deletePage(@PathVariable Long projectId, @PathVariable Long pageId) {
        pageService.deletePage(pageId);
        return ResponseEntity.noContent().build();
    }
}
