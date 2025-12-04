package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.WithdrawalStatus;
import com.sketchnotes.identityservice.model.WithdrawalRequest;
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
}
