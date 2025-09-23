package com.sketchnotes.payment_service.specification;

import com.sketchnotes.payment_service.entity.Transaction;
import com.sketchnotes.payment_service.entity.enumeration.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;


import java.util.ArrayList;
import java.util.List;


public class TransactionSpecification {
    public static Specification<Transaction> filter(Long walletId, PaymentStatus status, String type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (walletId != null) {
                predicates.add(cb.equal(root.get("walletId"), walletId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
