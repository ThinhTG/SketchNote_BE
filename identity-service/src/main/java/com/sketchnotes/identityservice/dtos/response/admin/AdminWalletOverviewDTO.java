package com.sketchnotes.identityservice.dtos.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho tổng quan Admin Wallet - Hiển thị số dư và thông tin cơ bản
 * Admin Wallet = Tiền thực sự Admin sở hữu (từ bán Subscription + Token)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletOverviewDTO {
    
    /**
     * Tổng số dư Admin Wallet (Revenue từ Subscription + Token)
     */
    private BigDecimal totalBalance;
    
    /**
     * Tổng tiền từ Subscription
     */
    private BigDecimal subscriptionBalance;
    
    /**
     * Tổng tiền từ Token/AI Credits
     */
    private BigDecimal tokenBalance;
    
    /**
     * Tổng tiền users đã nạp vào hệ thống (để tham khảo, không thuộc Admin)
     */
    private BigDecimal totalUserDeposits;
    
    /**
     * Tổng tiền users đã rút khỏi hệ thống (để tham khảo)
     */
    private BigDecimal totalUserWithdrawals;
    
    /**
     * Tổng tiền hiện có trong tất cả ví user (liability - nghĩa vụ phải trả)
     */
    private BigDecimal totalUserWalletBalance;
}
