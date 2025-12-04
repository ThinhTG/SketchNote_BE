package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
