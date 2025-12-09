package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.BankAccountRequest;
import com.sketchnotes.identityservice.dtos.response.BankAccountResponse;
import com.sketchnotes.identityservice.service.interfaces.IBankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank-accounts")
@Tag(name = "Bank Account", description = "Bank Account Management APIs")
public class BankAccountController {
    
    private final IBankAccountService bankAccountService;
    
    @PostMapping
    @Operation(summary = "Create a new bank account", description = "Create a new bank account for the current user")
    public ResponseEntity<ApiResponse<BankAccountResponse>> createBankAccount(
            @Valid @RequestBody BankAccountRequest request) {
        BankAccountResponse response = bankAccountService.createBankAccount(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Bank account created successfully", response));
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get all bank accounts current user", description = "Get all active bank accounts for the current user")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getMyBankAccounts() {
        List<BankAccountResponse> response = bankAccountService.getMyBankAccounts();
        return ResponseEntity.ok(new ApiResponse<>(200, "Bank accounts retrieved successfully", response));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bank accounts by user ID", description = "Get all active bank accounts for a specific user")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getBankAccountsByUserId(@PathVariable Long userId) {
        List<BankAccountResponse> response = bankAccountService.getBankAccountsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Bank accounts retrieved successfully", response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get bank account by ID", description = "Get a specific bank account by ID (must belong to current user)")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankAccountById(@PathVariable Long id) {
        BankAccountResponse response = bankAccountService.getBankAccountById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Bank account retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update bank account", description = "Update a bank account (must belong to current user)")
    public ResponseEntity<ApiResponse<BankAccountResponse>> updateBankAccount(
            @PathVariable Long id,
            @Valid @RequestBody BankAccountRequest request) {
        BankAccountResponse response = bankAccountService.updateBankAccount(id, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Bank account updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bank account", description = "Delete a bank account (soft delete)")
    public ResponseEntity<ApiResponse<String>> deleteBankAccount(@PathVariable Long id) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Bank account deleted successfully", null));
    }
    @GetMapping("/default")
    @Operation(summary = "Get default bank account", description = "Get default bank account for current user")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getDefaultBankAccount() {
        BankAccountResponse response = bankAccountService.getDefaultBankAccount();
        return ResponseEntity.ok(new ApiResponse<>(200, "Default bank account retrieved successfully", response));
    }
    @PostMapping("/{id}/set-default")
    @Operation(summary = "Set default bank account", description = "Set a bank account as default (unset others)")
    public ResponseEntity<ApiResponse<BankAccountResponse>> setDefaultBankAccount(@PathVariable Long id) {
        BankAccountResponse response = bankAccountService.setDefaultBankAccount(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Bank account set as default successfully", response));
    }
}
