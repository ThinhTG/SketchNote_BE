package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity.
 * Provides CRUD operations and custom queries for notification management.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a specific user, ordered by creation date (newest first).
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of notifications
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find notifications by user ID and read status.
     *
     * @param userId the user ID
     * @param isRead the read status
     * @return list of matching notifications
     */
    List<Notification> findByUserIdAndIsRead(Long userId, boolean isRead);
    
    /**
     * Count notifications by user ID and read status.
     *
     * @param userId the user ID
     * @param isRead the read status
     * @return count of matching notifications
     */
    long countByUserIdAndIsRead(Long userId, boolean isRead);
}
