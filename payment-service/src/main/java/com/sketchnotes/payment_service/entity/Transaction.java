package com.sketchnotes.payment_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sketchnotes.payment_service.entity.enumeration.PaymentStatus;
import com.sketchnotes.payment_service.entity.enumeration.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;


    private Long orderId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;   // DEPOSIT, PAYMENT, WITHDRAW

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;   // PENDING, SUCCESS, FAILED

    private String provider; // MOMO, PAYOS

    private String externalTransactionId; // transactionId từ MoMo/PayOS


    private LocalDateTime createdAt = LocalDateTime.now();



    // orderCode is only required for gateway callbacks (PayOS). Make it nullable
    // so other transaction types (DEPOSIT, WITHDRAW, PAYMENT from wallet) can be saved.
    @Column(unique = true)
    private Long orderCode;    // orderCode mapping với PayOS.

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    @JsonIgnore
    private Wallet wallet;
    // getters and setters
}
