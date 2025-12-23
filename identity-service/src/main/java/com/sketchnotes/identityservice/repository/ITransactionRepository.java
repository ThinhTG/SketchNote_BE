package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction,Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> getTransactionByWalletWalletId(long walletWalletId);
    
    Page<Transaction> findByWalletWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    
    Optional<Transaction> findByOrderCode(Long orderCode);
    
    /**
     * Tìm tất cả transactions PENDING có orderCode (PayOS) và đã tạo trước thời điểm cutoff.
     * Dùng cho scheduled job cleanup pending transactions.
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = :status " +
           "AND t.orderCode IS NOT NULL " +
           "AND t.createdAt < :cutoffTime")
    List<Transaction> findPendingTransactionsBeforeCutoff(
            @Param("status") PaymentStatus status,
            @Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Admin: filter by type
    Page<Transaction> findByType(TransactionType type, Pageable pageable);
    
    // Admin: search by user email, description, externalTransactionId
    @Query("SELECT t FROM Transaction t JOIN t.wallet w JOIN w.user u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.externalTransactionId) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Transaction> searchByKeyword(@Param("search") String search, Pageable pageable);
    
    // Admin: search + filter type
    @Query("SELECT t FROM Transaction t JOIN t.wallet w JOIN w.user u WHERE " +
           "t.type = :type AND (" +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.externalTransactionId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Transaction> searchByKeywordAndType(@Param("search") String search, @Param("type") TransactionType type, Pageable pageable);
    
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
