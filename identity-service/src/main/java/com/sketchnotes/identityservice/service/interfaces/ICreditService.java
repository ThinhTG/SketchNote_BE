package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.PurchaseCreditRequest;
import com.sketchnotes.identityservice.dtos.request.UseCreditRequest;
import com.sketchnotes.identityservice.dtos.response.CreditBalanceResponse;
import com.sketchnotes.identityservice.dtos.response.CreditTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface cho Credit Service
 */
public interface ICreditService {
    
    /**
     * Mua credit
     */
    CreditBalanceResponse purchaseCredits(Long userId, PurchaseCreditRequest request);
    
    /**
     * Sử dụng credit (được gọi từ AI service)
     */
    CreditBalanceResponse useCredits(Long userId, UseCreditRequest request);
    
    /**
     * Kiểm tra xem user có đủ credit không
     */
    boolean hasEnoughCredits(Long userId, Integer requiredAmount);
    
    /**
     * Lấy thông tin credit balance của user
     */
    CreditBalanceResponse getCreditBalance(Long userId);
    
    /**
     * Lấy lịch sử giao dịch credit
     */
    Page<CreditTransactionResponse> getCreditHistory(Long userId, Pageable pageable);
    
    /**
     * Tặng credit miễn phí cho user mới
     */
    void grantInitialCredits(Long userId, Integer amount);
}
