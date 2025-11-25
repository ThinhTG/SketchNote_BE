package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.MessageRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateMessageRequest;
import com.sketchnotes.identityservice.dtos.response.ConversationResponse;
import com.sketchnotes.identityservice.dtos.response.MessageResponse;
import com.sketchnotes.identityservice.service.interfaces.MessageService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody MessageRequest request) {
        MessageResponse response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Message sent successfully"));
    }

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getConversation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<MessageResponse> response = messageService.getConversation(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Conversation retrieved successfully"));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getAllConversations() {
        List<ConversationResponse> response = messageService.getAllConversations();
        return ResponseEntity.ok(ApiResponse.success(response, "Conversations retrieved successfully"));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessageById(@PathVariable Long messageId) {
        String currentUserKeycloakId = SecurityUtils.getCurrentUserId();
        MessageResponse response = messageService.getMessageById(messageId);
        return ResponseEntity.ok(ApiResponse.success(response, "Message retrieved successfully"));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateMessageRequest request) {
        String currentUserKeycloakId = SecurityUtils.getCurrentUserId();
        MessageResponse response = messageService.updateMessage(messageId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Message updated successfully"));
    }


    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<String>> deleteMessage(@PathVariable Long messageId) {
        String currentUserKeycloakId = SecurityUtils.getCurrentUserId();
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted successfully"));
    }
}
