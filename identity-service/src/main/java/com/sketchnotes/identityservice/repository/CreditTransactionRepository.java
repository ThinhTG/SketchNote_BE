package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.CreditTransactionType;
import com.sketchnotes.identityservice.model.CreditTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho CreditTransaction entity
 */
@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {
    
    /**
     * Tìm tất cả giao dịch của một user, sắp xếp theo thời gian giảm dần
     */
    Page<CreditTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Tìm giao dịch theo user và type
     */
    List<CreditTransaction> findByUserIdAndType(Long userId, CreditTransactionType type);
    
    /**
     * Tính tổng credit đã sử dụng (USAGE)
     */
    @Query("SELECT COALESCE(SUM(ABS(ct.amount)), 0) FROM CreditTransaction ct " +
           "WHERE ct.user.id = :userId AND ct.type = 'USAGE'")
    Integer getTotalCreditsUsed(@Param("userId") Long userId);
    
    /**
     * Tính tổng credit đã mua (PURCHASE + INITIAL_BONUS + BONUS)
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct " +
           "WHERE ct.user.id = :userId AND ct.type IN ('PURCHASE', 'INITIAL_BONUS', 'BONUS')")
    Integer getTotalCreditsPurchased(@Param("userId") Long userId);
    
    /**
     * Đếm số lần sử dụng AI của user
     */
    @Query("SELECT COUNT(ct) FROM CreditTransaction ct " +
           "WHERE ct.user.id = :userId AND ct.type = 'USAGE'")
    Long countAiUsage(@Param("userId") Long userId);
}
