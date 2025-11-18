package com.sketchnotes.order_service.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DashboardRepository {

    @PersistenceContext
    private EntityManager em;

    public BigDecimal totalRevenueForDesigner(Long designerId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COALESCE(SUM(od.subtotal_amount),0) FROM orders o " +
                "JOIN order_details od ON o.order_id = od.order_id " +
                "JOIN resource_template rt ON od.resource_template_id = rt.template_id " +
                "WHERE rt.designer_id = :designerId " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status IN ('CONFIRMED','DELIVERED') " +
                "AND o.issue_date BETWEEN :start AND :end";

        Query q = em.createNativeQuery(sql);
        q.setParameter("designerId", designerId);
        q.setParameter("start", start);
        q.setParameter("end", end);
        Object single = q.getSingleResult();
        if (single == null) return BigDecimal.ZERO;
        return new BigDecimal(single.toString());
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findTopTemplates(Long designerId, LocalDateTime start, LocalDateTime end, int limit) {
        String sql = "SELECT rt.template_id, rt.name, COUNT(*) as sold_count, COALESCE(SUM(od.subtotal_amount),0) as revenue " +
                "FROM order_details od " +
                "JOIN orders o ON od.order_id = o.order_id " +
                "JOIN resource_template rt ON od.resource_template_id = rt.template_id " +
                "WHERE rt.designer_id = :designerId " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status IN ('CONFIRMED','DELIVERED') " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY rt.template_id, rt.name " +
                "ORDER BY sold_count DESC " +
                "LIMIT :limit";

        Query q = em.createNativeQuery(sql);
        q.setParameter("designerId", designerId);
        q.setParameter("start", start);
        q.setParameter("end", end);
        q.setParameter("limit", limit);
        return (List<Object[]>) q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> salesByDay(Long designerId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY-MM-DD') as period, COUNT(*) as sold_count, COALESCE(SUM(od.subtotal_amount),0) as revenue " +
                "FROM orders o " +
                "JOIN order_details od ON o.order_id = od.order_id " +
                "JOIN resource_template rt ON od.resource_template_id = rt.template_id " +
                "WHERE rt.designer_id = :designerId " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status IN ('CONFIRMED','DELIVERED') " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period " +
                "ORDER BY period";

        Query q = em.createNativeQuery(sql);
        q.setParameter("designerId", designerId);
        q.setParameter("start", start);
        q.setParameter("end", end);
        return (List<Object[]>) q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> salesByMonth(Long designerId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY-MM') as period, COUNT(*) as sold_count, COALESCE(SUM(od.subtotal_amount),0) as revenue " +
                "FROM orders o " +
                "JOIN order_details od ON o.order_id = od.order_id " +
                "JOIN resource_template rt ON od.resource_template_id = rt.template_id " +
                "WHERE rt.designer_id = :designerId " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status IN ('CONFIRMED','DELIVERED') " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period " +
                "ORDER BY period";

        Query q = em.createNativeQuery(sql);
        q.setParameter("designerId", designerId);
        q.setParameter("start", start);
        q.setParameter("end", end);
        return (List<Object[]>) q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> salesByYear(Long designerId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT to_char(o.issue_date,'YYYY') as period, COUNT(*) as sold_count, COALESCE(SUM(od.subtotal_amount),0) as revenue " +
                "FROM orders o " +
                "JOIN order_details od ON o.order_id = od.order_id " +
                "JOIN resource_template rt ON od.resource_template_id = rt.template_id " +
                "WHERE rt.designer_id = :designerId " +
                "AND o.payment_status = 'PAID' " +
                "AND o.order_status IN ('CONFIRMED','DELIVERED') " +
                "AND o.issue_date BETWEEN :start AND :end " +
                "GROUP BY period " +
                "ORDER BY period";

        Query q = em.createNativeQuery(sql);
        q.setParameter("designerId", designerId);
        q.setParameter("start", start);
        q.setParameter("end", end);
        return (List<Object[]>) q.getResultList();
    }
}
