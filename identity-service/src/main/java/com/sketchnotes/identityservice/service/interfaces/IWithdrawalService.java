package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.RejectWithdrawalRequest;
import com.sketchnotes.identityservice.dtos.request.WithdrawalRequestDto;
import com.sketchnotes.identityservice.dtos.response.WithdrawalResponse;

import java.util.List;

/**
 * Service interface for withdrawal request management.
 */
public interface IWithdrawalService {
    
    /**
     * Customer creates a withdrawal request.
     *
     * @param userId the user ID
     * @param request the withdrawal request DTO
     * @return the created withdrawal response
     */
    WithdrawalResponse createWithdrawalRequest(Long userId, WithdrawalRequestDto request);
    
    /**
     * Get withdrawal history for a user.
     *
     * @param userId the user ID
     * @return list of withdrawal responses
     */
    List<WithdrawalResponse> getWithdrawalHistory(Long userId);
    
    /**
     * Staff approves a withdrawal request.
     *
     * @param withdrawalId the withdrawal request ID
     * @param staffId the staff ID who is approving
     * @return the updated withdrawal response
     */
    WithdrawalResponse approveWithdrawal(Long withdrawalId, Long staffId);
    
    /**
     * Staff rejects a withdrawal request.
     *
     * @param withdrawalId the withdrawal request ID
     * @param staffId the staff ID who is rejecting
     * @param request the rejection request with optional reason
     * @return the updated withdrawal response
     */
    WithdrawalResponse rejectWithdrawal(Long withdrawalId, Long staffId, RejectWithdrawalRequest request);
    
    /**
     * Get all pending withdrawal requests (for staff).
     *
     * @return list of pending withdrawal responses
     */
    List<WithdrawalResponse> getPendingWithdrawals();
    
    /**
     * Get all withdrawal requests (for admin/staff).
     *
     * @return list of all withdrawal responses
     */
    List<WithdrawalResponse> getAllWithdrawals();
}
