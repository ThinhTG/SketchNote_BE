package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.model.Transaction;

import java.util.List;

public interface ITransactionService {
    List<Transaction> filterTransactions(Long walletId, PaymentStatus status, String type) ;
}
