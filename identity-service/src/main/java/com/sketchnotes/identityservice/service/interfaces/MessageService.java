package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.MessageRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateMessageRequest;
import com.sketchnotes.identityservice.dtos.response.ConversationResponse;
import com.sketchnotes.identityservice.dtos.response.MessageResponse;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {
    
    /**
     * Send a new message
     */
    MessageResponse sendMessage(MessageRequest request);
    
    /**
     * Get conversation between current user and another user
     */
    PagedResponse<MessageResponse> getConversation(Long otherUserId, int page, int size);
    
    /**
     * Get all conversations for current user
     */
    List<ConversationResponse> getAllConversations( );
    
    /**
     * Update a message (only sender can update)
     */
    MessageResponse updateMessage(Long messageId, UpdateMessageRequest request);
    
    /**
     * Delete a message (soft delete, only sender can delete)
     */
    void deleteMessage(Long messageId);
    
    /**
     * Get a specific message by ID
     */
    MessageResponse getMessageById(Long messageId);
}
