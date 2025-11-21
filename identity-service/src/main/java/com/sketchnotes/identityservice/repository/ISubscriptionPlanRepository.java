package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.PlanType;
import com.sketchnotes.identityservice.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    
    List<SubscriptionPlan> findByIsActiveTrue();
    
    Optional<SubscriptionPlan> findByPlanType(PlanType planType);
    
    List<SubscriptionPlan> findByPlanTypeAndIsActiveTrue(PlanType planType);
}
