package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find messages between two users (conversation)
    @Query(value = "SELECT * FROM message m WHERE " +
           "((m.sender_id = :userId1 AND m.receiver_id = :userId2) OR " +
           "(m.sender_id = :userId2 AND m.receiver_id = :userId1)) " +
           "AND m.deleted_at IS NULL " +
           "ORDER BY m.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM message m WHERE " +
           "((m.sender_id = :userId1 AND m.receiver_id = :userId2) OR " +
           "(m.sender_id = :userId2 AND m.receiver_id = :userId1)) " +
           "AND m.deleted_at IS NULL",
           nativeQuery = true)
    Page<Message> findConversationBetweenUsers(
        @Param("userId1") Long userId1, 
        @Param("userId2") Long userId2, 
        Pageable pageable
    );
    
    // Find all users who sent messages to current user
    @Query(value = "SELECT DISTINCT m.sender_id FROM message m WHERE " +
           "m.receiver_id = :userId AND m.deleted_at IS NULL", nativeQuery = true)
    List<Long> findUserIdsSentToMe(@Param("userId") Long userId);
    
    // Find all users who received messages from current user
    @Query(value = "SELECT DISTINCT m.receiver_id FROM message m WHERE " +
           "m.sender_id = :userId AND m.deleted_at IS NULL", nativeQuery = true)
    List<Long> findUserIdsReceivedFromMe(@Param("userId") Long userId);
    
    // Find last message between two users
    @Query(value = "SELECT * FROM message m WHERE " +
           "((m.sender_id = :userId1 AND m.receiver_id = :userId2) OR " +
           "(m.sender_id = :userId2 AND m.receiver_id = :userId1)) " +
           "AND m.deleted_at IS NULL " +
           "ORDER BY m.created_at DESC " +
           "LIMIT 1", nativeQuery = true)
    Optional<Message> findLastMessageBetweenUsers(
        @Param("userId1") Long userId1, 
        @Param("userId2") Long userId2
    );

    // Find message by id and check if user is sender or receiver
    @Query(value = "SELECT * FROM message m WHERE m.id = :messageId AND " +
           "(m.sender_id = :userId OR m.receiver_id = :userId) " +
           "AND m.deleted_at IS NULL", nativeQuery = true)
    Optional<Message> findByIdAndUserId(
        @Param("messageId") Long messageId, 
        @Param("userId") Long userId
    );
}
