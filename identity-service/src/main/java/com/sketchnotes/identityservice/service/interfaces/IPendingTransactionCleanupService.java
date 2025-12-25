package com.sketchnotes.identityservice.service.interfaces;

/**
 * Service interface for cleanup of pending transactions.
 * Handles transactions that remain in PENDING status after payment link expiration.
 */
public interface IPendingTransactionCleanupService {
    
    /**
     * Cleanup pending transactions that have exceeded the timeout period.
     * This method marks expired transactions as FAILED.
     */
    void cleanupPendingTransactions();
}
