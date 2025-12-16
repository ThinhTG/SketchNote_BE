package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.request.BankAccountRequest;
import com.sketchnotes.identityservice.dtos.response.BankAccountResponse;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.mapper.BankAccountMapper;
import com.sketchnotes.identityservice.model.BankAccount;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.IBankAccountRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IBankAccountService;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService implements IBankAccountService {
    
    private final IBankAccountRepository bankAccountRepository;
    private final IUserRepository userRepository;
    private final BankAccountMapper bankAccountMapper;
    
    @Override
    @Transactional
    public BankAccountResponse createBankAccount(BankAccountRequest request) {
        // Get current user
        String keycloakId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (bankAccountRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(user.getId(), request.getAccountNumber())) {
            throw new AppException(ErrorCode.ACCOUNT_NUMBER_ALREADY_EXISTS);
        }

        long existingAccountsCount = bankAccountRepository.countByUserIdAndIsActiveTrue(user.getId());
        boolean shouldBeDefault = existingAccountsCount == 0 || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault) {
            bankAccountRepository.unsetAllDefaultForUser(user.getId());
        }
        
        // Create bank account
        BankAccount bankAccount = BankAccount.builder()
                .user(user)
                .logoUrl(request.getLogoUrl())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .branch(request.getBranch())
                .isDefault(shouldBeDefault)
                .isActive(true)
                .build();
        
        BankAccount saved = bankAccountRepository.save(bankAccount);
        log.info("Created bank account {} for user {}", saved.getId(), user.getId());
        
        return bankAccountMapper.toResponse(saved);
    }
    
    @Override
    public List<BankAccountResponse> getMyBankAccounts() {
        String keycloakId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        List<BankAccount> bankAccounts = bankAccountRepository.findByUserIdAndIsActiveTrue(user.getId());
        
        return bankAccounts.stream()
                .map(bankAccountMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BankAccountResponse> getBankAccountsByUserId(Long userId) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        List<BankAccount> bankAccounts = bankAccountRepository.findByUserIdAndIsActiveTrue(userId);
        
        log.info("Retrieved {} bank accounts for user {}", bankAccounts.size(), userId);
        
        return bankAccounts.stream()
                .map(bankAccountMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public BankAccountResponse getBankAccountById(Long id) {
        String keycloakId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        BankAccount bankAccount = bankAccountRepository.findByIdAndUserIdAndIsActiveTrue(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        
        if (!bankAccount.getIsActive()) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND);
        }
        
        return bankAccountMapper.toResponse(bankAccount);
    }
    
    @Override
    @Transactional
    public BankAccountResponse updateBankAccount(Long id, BankAccountRequest request) {
        String keycloakId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        BankAccount bankAccount = bankAccountRepository.findByIdAndUserIdAndIsActiveTrue(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        
        if (!bankAccount.getIsActive()) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND);
        }
        
        // Check if account number is being changed and if it already exists
        if (!bankAccount.getAccountNumber().equals(request.getAccountNumber())) {
            if (bankAccountRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(user.getId(), request.getAccountNumber())) {
                throw new AppException(ErrorCode.ACCOUNT_NUMBER_ALREADY_EXISTS);
            }
        }
        
        // Update fields
        bankAccount.setLogoUrl(request.getLogoUrl());
        bankAccount.setBankName(request.getBankName());
        bankAccount.setAccountNumber(request.getAccountNumber());
        bankAccount.setAccountHolderName(request.getAccountHolderName());
        bankAccount.setBranch(request.getBranch());
        
        // Handle default flag
        if (Boolean.TRUE.equals(request.getIsDefault()) && !bankAccount.getIsDefault()) {
            bankAccountRepository.unsetDefaultForUser(user.getId(), id);
            bankAccount.setIsDefault(true);
        } else if (Boolean.FALSE.equals(request.getIsDefault()) && bankAccount.getIsDefault()) {
            bankAccount.setIsDefault(false);
        }
        
        BankAccount updated = bankAccountRepository.save(bankAccount);
        log.info("Updated bank account {} for user {}", id, user.getId());
        
        return bankAccountMapper.toResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteBankAccount(Long id) {
        String keycloakId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        BankAccount bankAccount = bankAccountRepository.findByIdAndUserIdAndIsActiveTrue(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        
        if (!bankAccount.getIsActive()) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND);
        }
        
        // Soft delete
        bankAccount.setIsActive(false);
        bankAccount.setIsDefault(false);
        bankAccountRepository.save(bankAccount);
        
        log.info("Deleted bank account {} for user {}", id, user.getId());
    }
    
    @Override
    @Transactional
    public BankAccountResponse setDefaultBankAccount(Long id) {
        String keycloakId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        BankAccount bankAccount = bankAccountRepository.findByIdAndUserIdAndIsActiveTrue(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        
        if (!bankAccount.getIsActive()) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND);
        }
        
        // Unset all other defaults
        bankAccountRepository.unsetDefaultForUser(user.getId(), id);
        
        // Set this as default
        bankAccount.setIsDefault(true);
        BankAccount updated = bankAccountRepository.save(bankAccount);
        
        log.info("Set bank account {} as default for user {}", id, user.getId());
        
        return bankAccountMapper.toResponse(updated);
    }
    
    @Override
    public BankAccountResponse getDefaultBankAccount() {
        String keycloakId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        BankAccount bankAccount = bankAccountRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NO_DEFAULT_BANK_ACCOUNT));
        
        return bankAccountMapper.toResponse(bankAccount);
    }
}
