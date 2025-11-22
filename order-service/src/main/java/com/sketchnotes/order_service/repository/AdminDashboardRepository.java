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

    @SuppressWarnings("unchecked")
    public List<Object[]> findTopSellingResources(int limit) {
        String sql = "SELECT od.resource_template_id, COUNT(od.order_detail_id) as sales_count, rt.name " +
                "FROM order_details od " +
                "JOIN orders o ON od.order_id = o.order_id " +
                "JOIN resource_template rt ON od.resource_template_id = rt.template_id " +
                "WHERE o.order_status = 'SUCCESS' " +
                "GROUP BY od.resource_template_id, rt.name " +
                "ORDER BY sales_count DESC " +
                "LIMIT :limit";
        Query q = em.createNativeQuery(sql);
        q.setParameter("limit", limit);
        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findTopDesignersByRevenue(int limit) {
        // Assuming resource_template has designer_id
        String sql = "SELECT rt.designer_id, SUM(od.subtotal_amount) as total_revenue " +
                "FROM order_details od " +
                "JOIN orders o ON od.order_id = o.order_id " +
                "JOIN resource_template rt ON od.resource_template_id = rt.template_id " +
                "WHERE o.order_status = 'SUCCESS' " +
                "GROUP BY rt.designer_id " +
                "ORDER BY total_revenue DESC " +
                "LIMIT :limit";
        Query q = em.createNativeQuery(sql);
        q.setParameter("limit", limit);
        return q.getResultList();
    }

    public long countSuccessfulOrders() {
        String sql = "SELECT COUNT(o) FROM orders o WHERE o.order_status = 'SUCCESS'";
        Query q = em.createNativeQuery(sql);
        return ((Number) q.getSingleResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> countSubscriptionsByPlan() {
        // Assuming subscription_id maps to a plan. If we need plan name, we might need another join or just return ID.
        // For now returning subscription_id and count.
        String sql = "SELECT o.subscription_id, COUNT(o.order_id) " +
                "FROM orders o " +
                "WHERE o.subscription_id IS NOT NULL " +
                "AND o.order_status = 'SUCCESS' " +
                "GROUP BY o.subscription_id";
        return em.createNativeQuery(sql).getResultList();
    }
}
