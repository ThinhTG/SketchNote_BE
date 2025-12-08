package com.sketchnotes.identityservice.mapper;

import com.sketchnotes.identityservice.dtos.response.BankAccountResponse;
import com.sketchnotes.identityservice.model.BankAccount;
import org.springframework.stereotype.Component;

@Component
public class BankAccountMapper {
    
    public BankAccountResponse toResponse(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }
        
        return BankAccountResponse.builder()
                .id(bankAccount.getId())
                .userId(bankAccount.getUser() != null ? bankAccount.getUser().getId() : null)
                .bankName(bankAccount.getBankName())
                .accountNumber(bankAccount.getAccountNumber())
                .accountHolderName(bankAccount.getAccountHolderName())
                .branch(bankAccount.getBranch())
                .isDefault(bankAccount.getIsDefault())
                .isActive(bankAccount.getIsActive())
                .createdAt(bankAccount.getCreatedAt())
                .updatedAt(bankAccount.getUpdatedAt())
                .build();
    }
}
