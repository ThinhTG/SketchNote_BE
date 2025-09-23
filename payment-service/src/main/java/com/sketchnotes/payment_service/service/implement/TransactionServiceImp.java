package com.sketchnotes.payment_service.service.implement;

import com.sketchnotes.payment_service.entity.Transaction;
import com.sketchnotes.payment_service.entity.enumeration.PaymentStatus;
import com.sketchnotes.payment_service.repository.TransactionRepository;
import com.sketchnotes.payment_service.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImp {

    private final TransactionRepository transactionRepository;

    public List<Transaction> filterTransactions(Long walletId, PaymentStatus status, String type) {
        return transactionRepository.findAll(
                TransactionSpecification.filter(walletId, status, type)
        );
    }
}
