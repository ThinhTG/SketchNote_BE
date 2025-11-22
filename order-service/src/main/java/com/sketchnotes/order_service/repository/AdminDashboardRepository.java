package com.sketchnotes.order_service.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AdminDashboardRepository {

    @PersistenceContext
    private EntityManager em;

    // Subscription Revenue
    @SuppressWarnings("unchecked")
    public List<Object[]> subscriptionRevenueByDay(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY-MM-DD') as period, COALESCE(SUM(o.total_amount),0) " +
                "FROM orders o " +
                "WHERE o.subscription_id IS NOT NULL " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status = 'SUCCESS' " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period ORDER BY period";
        return executeQuery(sql, start, end);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> subscriptionRevenueByMonth(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY-MM') as period, COALESCE(SUM(o.total_amount),0) " +
                "FROM orders o " +
                "WHERE o.subscription_id IS NOT NULL " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status = 'SUCCESS' " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period ORDER BY period";
        return executeQuery(sql, start, end);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> subscriptionRevenueByYear(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY') as period, COALESCE(SUM(o.total_amount),0) " +
                "FROM orders o " +
                "WHERE o.subscription_id IS NOT NULL " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status = 'SUCCESS' " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period ORDER BY period";
        return executeQuery(sql, start, end);
    }

    // Resource Revenue (Total revenue from resource sales)
    @SuppressWarnings("unchecked")
//    @SuppressWarnings("unchecked")
    public List<Object[]> resourceRevenueByDay(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY-MM-DD') as period, COALESCE(SUM(od.subtotal_amount),0) " +
                "FROM orders o " +
                "JOIN order_details od ON o.order_id = od.order_id " +
                "WHERE o.subscription_id IS NULL " + 
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status = 'SUCCESS' " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period ORDER BY period";
        return executeQuery(sql, start, end);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> resourceRevenueByMonth(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY-MM') as period, COALESCE(SUM(od.subtotal_amount),0) " +
                "FROM orders o " +
                "JOIN order_details od ON o.order_id = od.order_id " +
                "WHERE o.subscription_id IS NULL " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status = 'SUCCESS' " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period ORDER BY period";
        return executeQuery(sql, start, end);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> resourceRevenueByYear(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY') as period, COALESCE(SUM(od.subtotal_amount),0) " +
                "FROM orders o " +
                "JOIN order_details od ON o.order_id = od.order_id " +
                "WHERE o.subscription_id IS NULL " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status = 'SUCCESS' " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period ORDER BY period";
        return executeQuery(sql, start, end);
    }

    private List<Object[]> executeQuery(String sql, LocalDateTime start, LocalDateTime end) {
        Query q = em.createNativeQuery(sql);
        q.setParameter("start", start);
        q.setParameter("end", end);
        return q.getResultList();
    }
}
