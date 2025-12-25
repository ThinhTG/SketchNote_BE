package com.sketchnotes.identityservice.model;

import com.sketchnotes.identityservice.enums.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a customer withdrawal request.
 */
@Entity
@Table(name = "withdrawal_request", indexes = {
    @Index(name = "idx_withdrawal_user", columnList = "user_id"),
    @Index(name = "idx_withdrawal_status", columnList = "status"),
    @Index(name = "idx_withdrawal_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who requested the withdrawal
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * Amount to withdraw
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    /**
     * Bank name (e.g., Vietcombank, Techcombank)
     */
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;
    
    /**
     * Bank account number
     */
    @Column(name = "bank_account_number", nullable = false, length = 50)
    private String bankAccountNumber;
    
    /**
     * Bank account holder name
     */
    @Column(name = "bank_account_holder", nullable = false, length = 200)
    private String bankAccountHolder;

    private String billImage;
    /**
     * Current status of the withdrawal request
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WithdrawalStatus status = WithdrawalStatus.PENDING;
    
    /**
     * Staff member who approved/rejected the request (nullable)
     */
    @Column(name = "staff_id")
    private Long staffId;
    
    /**
     * Optional rejection reason
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    /**
     * Timestamp when the request was created
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the request was last updated
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
