package com.sketchnotes.payment_service.repository;

import com.sketchnotes.payment_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> getTransactionByWalletWalletId(long walletWalletId);
    Optional<Transaction> findByOrderCode(Long orderCode);

}
