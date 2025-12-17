package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.response.TransactionHistoryPagedResponse;
import com.sketchnotes.identityservice.dtos.response.TransactionHistoryResponse;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.repository.ITransactionRepository;
import com.sketchnotes.identityservice.repository.IWalletRepository;
import com.sketchnotes.identityservice.service.interfaces.ITransactionService;
import com.sketchnotes.identityservice.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImp implements ITransactionService {

    private final ITransactionRepository transactionRepository;
    private final IWalletRepository walletRepository;

    public List<Transaction> filterTransactions(Long walletId, PaymentStatus status, String type) {
        return transactionRepository.findAll(
                TransactionSpecification.filter(walletId, status, type)
        );
    }

    @Override
    public TransactionHistoryPagedResponse getTransactionHistoryByUserId(
            Long userId,
            TransactionType type,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        
        log.info("Getting transaction history for user {}: page={}, size={}, type={}, sortBy={}, sortDir={}",
                userId, page, size, type, sortBy, sortDir);
        
        // Lấy wallet của user
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        
        // Tạo Pageable với sort
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Lấy transactions theo wallet và type (nếu có)
        Page<Transaction> transactionPage;
        if (type != null) {
            // Filter theo walletId và type
            Specification<Transaction> spec = Specification.where(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("wallet").get("walletId"), wallet.getWalletId()),
                            cb.equal(root.get("type"), type)
                    )
            );
            transactionPage = transactionRepository.findAll(spec, pageable);
        } else {
            // Chỉ filter theo walletId
            transactionPage = transactionRepository.findByWalletWalletIdOrderByCreatedAtDesc(wallet.getWalletId(), pageable);
        }
        
        // Map transactions to response DTOs
        List<TransactionHistoryResponse> transactionResponses = transactionPage.getContent().stream()
                .map(this::mapToTransactionHistoryResponse)
                .collect(Collectors.toList());
        
        // Build response với balance và pagination
        return TransactionHistoryPagedResponse.builder()
                .currentBalance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .transactions(transactionResponses)
                .pageNo(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .isLast(transactionPage.isLast())
                .build();
    }
    
    private TransactionHistoryResponse mapToTransactionHistoryResponse(Transaction transaction) {
        return TransactionHistoryResponse.builder()
                .transactionId(transaction.getTransactionId())
                .orderId(transaction.getOrderId())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalance())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .provider(transaction.getProvider())
                .externalTransactionId(transaction.getExternalTransactionId())
                .description(transaction.getDescription())
                .orderCode(transaction.getOrderCode())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
