package com.sketchnotes.identityservice.enums;

/**
 * Enum định nghĩa các loại giao dịch credit
 */
public enum CreditTransactionType {
    PURCHASE,           // Mua credit (theo số lượng)
    PACKAGE_PURCHASE,   // Mua gói credit package
    USAGE,              // Sử dụng credit (AI generation)
    REFUND,             // Hoàn lại credit
    BONUS,              // Thưởng credit (promotion)
    INITIAL_BONUS       // Credit miễn phí ban đầu
}
