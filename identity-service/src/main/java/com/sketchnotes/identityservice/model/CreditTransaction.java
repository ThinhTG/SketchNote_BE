package com.sketchnotes.identityservice.model;

import com.sketchnotes.identityservice.enums.CreditTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ lịch sử giao dịch credit của người dùng
 */
@Entity
@Table(name = "credit_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditTransactionType type;
    
    @Column(nullable = false)
    private Integer amount; // Số lượng credit (dương: thêm, âm: trừ)
    
    @Column(nullable = false)
    private Integer balanceAfter; // Số dư sau giao dịch
    
    private String description; // Mô tả giao dịch
    
    private String referenceId; // ID tham chiếu (ví dụ: orderId, imageId)
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
