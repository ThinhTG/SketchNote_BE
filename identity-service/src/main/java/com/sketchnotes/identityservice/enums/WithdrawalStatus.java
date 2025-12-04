package com.sketchnotes.identityservice.enums;

/**
 * Status enum for withdrawal requests.
 */
public enum WithdrawalStatus {
    /**
     * Request has been submitted and is pending review
     */
    PENDING,
    
    /**
     * Request has been approved and money transferred
     */
    APPROVED,
    
    /**
     * Request has been rejected
     */
    REJECTED
}
