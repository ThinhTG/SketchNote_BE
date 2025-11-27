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
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1)) " +
           "AND m.deletedAt IS NULL")
    Page<Message> findConversationBetweenUsers(
        @Param("userId1") Long userId1, 
        @Param("userId2") Long userId2, 
        Pageable pageable
    );
    
    // Find all users who sent messages to current user
    @Query("SELECT DISTINCT m.sender.id FROM Message m WHERE " +
           "m.receiver.id = :userId AND m.deletedAt IS NULL")
    List<Long> findUserIdsSentToMe(@Param("userId") Long userId);
    
    // Find all users who received messages from current user
    @Query("SELECT DISTINCT m.receiver.id FROM Message m WHERE " +
           "m.sender.id = :userId AND m.deletedAt IS NULL")
    List<Long> findUserIdsReceivedFromMe(@Param("userId") Long userId);
    
    // Find last message between two users
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1)) " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC")
    List<Message> findLastMessageBetweenUsersList(
        @Param("userId1") Long userId1, 
        @Param("userId2") Long userId2
    );

    // Find message by id and check if user is sender or receiver
    @Query("SELECT m FROM Message m WHERE m.id = :messageId AND " +
           "(m.sender.id = :userId OR m.receiver.id = :userId) " +
           "AND m.deletedAt IS NULL")
    Optional<Message> findByIdAndUserId(
        @Param("messageId") Long messageId, 
        @Param("userId") Long userId
    );
}
