package com.sketchnotes.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "resource_template_id")
    private Long resourceTemplateId; // For single template orders

    @Column(name = "subscription_id")
    private Long subscriptionId; // For subscription orders

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus; // PENDING, PAID, FAILED, REFUNDED

    @Column(name = "order_status", length = 50)
    private String orderStatus; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
        if (orderStatus == null) {
            orderStatus = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

