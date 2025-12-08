package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.BankAccountRequest;
import com.sketchnotes.identityservice.dtos.response.BankAccountResponse;

import java.util.List;

public interface IBankAccountService {
    
    /**
     * Create a new bank account for the current user
     */
    BankAccountResponse createBankAccount(BankAccountRequest request);
    
    /**
     * Get all bank accounts for the current user
     */
    List<BankAccountResponse> getMyBankAccounts();
    
    /**
     * Get all bank accounts for a specific user (admin only)
     */
    List<BankAccountResponse> getBankAccountsByUserId(Long userId);
    
    /**
     * Get a specific bank account by ID (must belong to current user)
     */
    BankAccountResponse getBankAccountById(Long id);
    
    /**
     * Update a bank account (must belong to current user)
     */
    BankAccountResponse updateBankAccount(Long id, BankAccountRequest request);
    
    /**
     * Delete a bank account (soft delete - set isActive to false)
     */
    void deleteBankAccount(Long id);
    
    /**
     * Set a bank account as default (unset others)
     */
    BankAccountResponse setDefaultBankAccount(Long id);
    
    /**
     * Get default bank account for current user
     */
    BankAccountResponse getDefaultBankAccount();
}
