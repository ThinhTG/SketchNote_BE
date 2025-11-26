package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.dtos.request.MessageRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateMessageRequest;
import com.sketchnotes.identityservice.dtos.response.ConversationResponse;
import com.sketchnotes.identityservice.dtos.response.MessageResponse;
import com.sketchnotes.identityservice.model.Message;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.repository.MessageRepository;
import com.sketchnotes.identityservice.service.interfaces.MessageService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public MessageResponse sendMessage(MessageRequest request) {
        String senderKeycloakId = SecurityUtils.getCurrentUserId();
        
        User sender = userRepository.findByKeycloakId(senderKeycloakId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found with id: " + request.getReceiverId()));
        
        // Don't allow sending message to yourself
        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Cannot send message to yourself");
        }
        
        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .build();
        
        Message saved = messageRepository.save(message);
        log.info("Message sent successfully with id: {}", saved.getId());
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MessageResponse> getConversation(Long otherUserId, int page, int size) {
        String currentUserKeycloakId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        User currentUser = userRepository.findByKeycloakId(currentUserKeycloakId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        // Verify other user exists
        userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + otherUserId));
        
        Page<Message> messagePage = messageRepository.findConversationBetweenUsers(
                currentUser.getId(), otherUserId, pageable);
        
        // Convert to DTOs
        List<MessageResponse> responses = messagePage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                responses,
                messagePage.getNumber(),
                messagePage.getSize(),
                (int) messagePage.getTotalElements(),
                messagePage.getTotalPages(),
                messagePage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllConversations() {
        String currentUserKeycloakId = SecurityUtils.getCurrentUserId();

        User currentUser = userRepository.findByKeycloakId(currentUserKeycloakId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Get user IDs who sent messages to me
        List<Long> senderIds = messageRepository.findUserIdsSentToMe(currentUser.getId());

        // Get user IDs who received messages from me
        List<Long> receiverIds = messageRepository.findUserIdsReceivedFromMe(currentUser.getId());

        // Merge and deduplicate using Set
        java.util.Set<Long> partnerIds = new java.util.HashSet<>();
        partnerIds.addAll(senderIds);
        partnerIds.addAll(receiverIds);

        // Batch fetch all users at once to avoid N+1 problem
        List<User> partners = userRepository.findAllById(partnerIds);
        java.util.Map<Long, User> partnerMap = new java.util.HashMap<>();
        for (User partner : partners) {
            partnerMap.put(partner.getId(), partner);
        }

        // Build conversation responses
        List<ConversationResponse> conversations = new ArrayList<>();
        for (Long partnerId : partnerIds) {
            User partner = partnerMap.get(partnerId);

            if (partner == null) {
                continue; // Skip if user not found
            }

            Message lastMessage = messageRepository.findLastMessageBetweenUsers(
                    currentUser.getId(), partner.getId())
                    .orElse(null);


            ConversationResponse conversation = ConversationResponse.builder()
                    .userId(partner.getId())
                    .userName(partner.getFirstName() + " " + partner.getLastName())
                    .userAvatarUrl(partner.getAvatarUrl())
                    .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                    .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                    .build();

            conversations.add(conversation);
        }

        conversations.sort((c1, c2) -> {
            if (c1.getLastMessageTime() == null) return 1;
            if (c2.getLastMessageTime() == null) return -1;
            return c2.getLastMessageTime().compareTo(c1.getLastMessageTime());
        });

        return conversations;
    }

    @Override
    @Transactional
    public MessageResponse updateMessage(Long messageId, UpdateMessageRequest request) {
        String senderKeycloakId = SecurityUtils.getCurrentUserId();
        
        User sender = userRepository.findByKeycloakId(senderKeycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Message message = messageRepository.findByIdAndUserId(messageId, sender.getId())
                .orElseThrow(() -> new RuntimeException("Message not found or you don't have permission"));
        
        // Only sender can update message
        if (!message.getSender().getId().equals(sender.getId())) {
            throw new RuntimeException("Only the sender can update the message");
        }
        
        message.setContent(request.getContent());
        Message updated = messageRepository.save(message);
        
        log.info("Message {} updated successfully", messageId);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        String senderKeycloakId = SecurityUtils.getCurrentUserId();
        
        User sender = userRepository.findByKeycloakId(senderKeycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Message message = messageRepository.findByIdAndUserId(messageId, sender.getId())
                .orElseThrow(() -> new RuntimeException("Message not found or you don't have permission"));
        
        // Only sender can delete message
        if (!message.getSender().getId().equals(sender.getId())) {
            throw new RuntimeException("Only the sender can delete the message");
        }
        
        // Soft delete
        message.setDeletedAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long messageId) {
        String userKeycloakId = SecurityUtils.getCurrentUserId();
        
        User user = userRepository.findByKeycloakId(userKeycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Message message = messageRepository.findByIdAndUserId(messageId, user.getId())
                .orElseThrow(() -> new RuntimeException("Message not found or you don't have permission"));
        
        return toDto(message);
    }

    private MessageResponse toDto(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .senderAvatarUrl(message.getSender().getAvatarUrl())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getFirstName() + " " + message.getReceiver().getLastName())
                .receiverAvatarUrl(message.getReceiver().getAvatarUrl())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
