package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.SubscriptionPlanRequest;
import com.sketchnotes.identityservice.dtos.response.SubscriptionPlanResponse;

import java.util.List;

public interface ISubscriptionPlanService {
    
    SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request);
    
    SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanRequest request);
    
    List<SubscriptionPlanResponse> getAllActivePlans();
    
    SubscriptionPlanResponse getPlanById(Long planId);
    
    void deactivatePlan(Long planId);
}
