package com.sketchnotes.order_service.service;

/**
 * Service interface for cleanup of pending orders.
 * Handles orders that remain in PENDING status after payment link expiration.
 */
public interface IPendingOrderCleanupService {
    
    /**
     * Cleanup pending orders that have exceeded the timeout period.
     * This method checks payment status and marks expired orders accordingly.
     */
    void cleanupPendingOrders();
}
