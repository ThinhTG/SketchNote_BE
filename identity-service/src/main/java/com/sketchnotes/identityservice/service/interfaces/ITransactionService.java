package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.response.TransactionHistoryPagedResponse;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.model.Transaction;

import java.util.List;

public interface ITransactionService {
    List<Transaction> filterTransactions(Long walletId, PaymentStatus status, String type);
    
    /**
     * Lấy lịch sử giao dịch của user với phân trang và balance hiện tại
     * @param userId ID của user
     * @param type Loại giao dịch (optional)
     * @param page Số trang (0-indexed)
     * @param size Số lượng phần tử mỗi trang
     * @param sortBy Field để sort
     * @param sortDir Hướng sort (asc/desc)
     * @return TransactionHistoryPagedResponse bao gồm balance và danh sách transactions
     */
    TransactionHistoryPagedResponse getTransactionHistoryByUserId(
            Long userId, 
            TransactionType type,
            int page, 
            int size, 
            String sortBy, 
            String sortDir
    );
}
