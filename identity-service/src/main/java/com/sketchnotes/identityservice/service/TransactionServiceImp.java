package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.repository.ITransactionRepository;
import com.sketchnotes.identityservice.service.interfaces.ITransactionService;
import com.sketchnotes.identityservice.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImp implements ITransactionService {

    private final ITransactionRepository transactionRepository;

    public List<Transaction> filterTransactions(Long walletId, PaymentStatus status, String type) {
        return transactionRepository.findAll(
                TransactionSpecification.filter(walletId, status, type)
        );
    }
}
