package com.sketchnotes.identityservice.specification;

import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class TransactionSpecification {
    public static Specification<Transaction> filter(Long walletId, PaymentStatus status, String type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (walletId != null) {
                predicates.add(cb.equal(root.get("wallet").get("walletId"), walletId));
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
