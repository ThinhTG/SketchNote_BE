package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction,Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> getTransactionByWalletWalletId(long walletWalletId);
    Optional<Transaction> findByOrderCode(Long orderCode);
    
    @org.springframework.data.jpa.repository.Query(value = "SELECT to_char(t.created_at, 'YYYY-MM-DD') as period, COALESCE(SUM(t.amount), 0) " +
            "FROM transaction t WHERE t.type = :type AND t.status = 'SUCCESS' " +
            "AND t.created_at BETWEEN :start AND :end " +
            "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> sumAmountByDay(String type, java.time.LocalDateTime start, java.time.LocalDateTime end);

    @org.springframework.data.jpa.repository.Query(value = "SELECT to_char(t.created_at, 'YYYY-MM') as period, COALESCE(SUM(t.amount), 0) " +
            "FROM transaction t WHERE t.type = :type AND t.status = 'SUCCESS' " +
            "AND t.created_at BETWEEN :start AND :end " +
            "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> sumAmountByMonth(String type, java.time.LocalDateTime start, java.time.LocalDateTime end);

    @org.springframework.data.jpa.repository.Query(value = "SELECT to_char(t.created_at, 'YYYY') as period, COALESCE(SUM(t.amount), 0) " +
            "FROM transaction t WHERE t.type = :type AND t.status = 'SUCCESS' " +
            "AND t.created_at BETWEEN :start AND :end " +
            "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> sumAmountByYear(String type, java.time.LocalDateTime start, java.time.LocalDateTime end);

}
