package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.WithdrawalStatus;
import com.sketchnotes.identityservice.model.WithdrawalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WithdrawalRequest entity.
 */
@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    
    /**
     * Find all withdrawal requests for a specific user, ordered by creation date descending.
     *
     * @param userId the user ID
     * @return list of withdrawal requests
     */
    List<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find all withdrawal requests for a specific user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of withdrawal requests
     */
    Page<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Check if a user has any pending withdrawal requests.
     *
     * @param userId the user ID
     * @param status the withdrawal status
     * @return true if pending request exists
     */
    boolean existsByUserIdAndStatus(Long userId, WithdrawalStatus status);
    
    /**
     * Find all withdrawal requests by status, ordered by creation date ascending.
     *
     * @param status the withdrawal status
     * @return list of withdrawal requests
     */
    List<WithdrawalRequest> findByStatusOrderByCreatedAtAsc(WithdrawalStatus status);
    
    /**
     * Find a withdrawal request by ID and user ID (for authorization).
     *
     * @param id the withdrawal request ID
     * @param userId the user ID
     * @return optional withdrawal request
     */
    Optional<WithdrawalRequest> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Search withdrawal requests with pagination.
     * Searches by bank name, bank account number, bank account holder, or status.
     *
     * @param search the search keyword
     * @param pageable pagination information
     * @return page of withdrawal requests
     */
    @Query("SELECT w FROM WithdrawalRequest w WHERE " +
           "LOWER(w.bankName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.bankAccountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.bankAccountHolder) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(w.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<WithdrawalRequest> searchWithdrawals(@Param("search") String search, Pageable pageable);
    
    /**
     * Find all withdrawal requests with pagination.
     *
     * @param pageable pagination information
     * @return page of withdrawal requests
     */
    Page<WithdrawalRequest> findAll(Pageable pageable);
    
    /**
     * Search by status with pagination.
     *
     * @param status the withdrawal status
     * @param pageable pagination information
     * @return page of withdrawal requests
     */
    Page<WithdrawalRequest> findByStatus(WithdrawalStatus status, Pageable pageable);
    
    /**
     * Search withdrawal requests by status with keyword search.
     *
     * @param search the search keyword
     * @param status the withdrawal status
     * @param pageable pagination information
     * @return page of withdrawal requests
     */
    @Query("SELECT w FROM WithdrawalRequest w WHERE w.status = :status AND " +
           "(LOWER(w.bankName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.bankAccountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.bankAccountHolder) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<WithdrawalRequest> searchWithdrawalsByStatus(@Param("search") String search, 
                                                       @Param("status") WithdrawalStatus status, 
                                                       Pageable pageable);
}
