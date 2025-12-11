package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    
    List<UserSubscription> findByUserAndStatus(User user, SubscriptionStatus status);
    
    List<UserSubscription> findByUser(User user);
    
    /**
     * Find all active subscriptions for a user (not expired)
     * Returns list because user might have multiple active subscriptions (edge case)
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.user = :user AND us.status = 'ACTIVE' AND us.endDate > :now ORDER BY us.startDate DESC")
    List<UserSubscription> findActiveSubscriptionsByUser(User user, LocalDateTime now);
    
    /**
     * Find the most recent active subscription for a user
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.user = :user AND us.status = 'ACTIVE' AND us.endDate > :now ORDER BY us.startDate DESC LIMIT 1")
    Optional<UserSubscription> findLatestActiveSubscriptionByUser(User user, LocalDateTime now);
    
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'ACTIVE' AND us.endDate < :now")
    List<UserSubscription> findExpiredSubscriptions(LocalDateTime now);
    
    // ==================== Admin Dashboard Methods ====================
    
    /**
     * Find subscriptions by status with pagination
     */
    Page<UserSubscription> findByStatus(SubscriptionStatus status, Pageable pageable);
    
    /**
     * Find subscriptions by planId with pagination
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.subscriptionPlan.planId = :planId")
    Page<UserSubscription> findByPlanId(@Param("planId") Long planId, Pageable pageable);
    
    /**
     * Find subscriptions by status and planId with pagination
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.status = :status AND us.subscriptionPlan.planId = :planId")
    Page<UserSubscription> findByStatusAndPlanId(@Param("status") SubscriptionStatus status, @Param("planId") Long planId, Pageable pageable);
    
    /**
     * Search subscriptions by keyword (user email, user name, plan name)
     */
    @Query("SELECT us FROM UserSubscription us JOIN us.user u JOIN us.subscriptionPlan sp WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sp.planName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<UserSubscription> searchByKeyword(@Param("search") String search, Pageable pageable);
    
    /**
     * Search subscriptions by keyword and status
     */
    @Query("SELECT us FROM UserSubscription us JOIN us.user u JOIN us.subscriptionPlan sp WHERE " +
           "us.status = :status AND (" +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sp.planName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserSubscription> searchByKeywordAndStatus(@Param("search") String search, @Param("status") SubscriptionStatus status, Pageable pageable);
    
    /**
     * Search subscriptions by keyword and planId
     */
    @Query("SELECT us FROM UserSubscription us JOIN us.user u JOIN us.subscriptionPlan sp WHERE " +
           "us.subscriptionPlan.planId = :planId AND (" +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sp.planName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserSubscription> searchByKeywordAndPlanId(@Param("search") String search, @Param("planId") Long planId, Pageable pageable);
    
    /**
     * Search subscriptions by keyword, status and planId
     */
    @Query("SELECT us FROM UserSubscription us JOIN us.user u JOIN us.subscriptionPlan sp WHERE " +
           "us.status = :status AND us.subscriptionPlan.planId = :planId AND (" +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sp.planName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserSubscription> searchByKeywordAndStatusAndPlanId(@Param("search") String search, @Param("status") SubscriptionStatus status, @Param("planId") Long planId, Pageable pageable);
    
    /**
     * Find subscriptions by user with pagination
     */
    Page<UserSubscription> findByUser(User user, Pageable pageable);
}
