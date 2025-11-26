package com.sketchnotes.identityservice.enums;

/**
 * Enum representing different types of notifications in the system.
 * Used to categorize notifications for filtering and display purposes.
 */
public enum NotificationType {
    /**
     * Notification sent to designer when their resource is purchased
     */
    PURCHASE,
    
    /**
     * Notification sent to buyer confirming successful purchase
     */
    PURCHASE_CONFIRM,
    
    /**
     * System-wide notifications (maintenance, updates, etc.)
     */
    SYSTEM,
    
    /**
     * Notifications related to comments on blogs or resources
     */
    COMMENT,
    
    /**
     * Notifications for course enrollments
     */
    ENROLLMENT,
    
    /**
     * Notifications related to subscription changes
     */
    SUBSCRIPTION,
    
    /**
     * Notifications for wallet transactions
     */
    WALLET
}
