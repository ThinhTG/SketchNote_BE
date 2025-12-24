package com.sketchnotes.learning.dto.enums;

public enum TransactionType {
    DEPOSIT,
    PAYMENT,
    WITHDRAW,
    COURSE_FEE,
    SUBSCRIPTION,
    // Specific purchase types to distinguish payment reasons
    PURCHASE_RESOURCE,
    PURCHASE_AI_CREDITS,
    PURCHASE_SUBSCRIPTION
}
