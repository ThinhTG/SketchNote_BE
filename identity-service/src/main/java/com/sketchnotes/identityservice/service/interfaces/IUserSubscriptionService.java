package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.PurchaseSubscriptionRequest;
import com.sketchnotes.identityservice.dtos.response.UserQuotaResponse;
import com.sketchnotes.identityservice.dtos.response.UserSubscriptionResponse;

import java.util.List;

public interface IUserSubscriptionService {
    
    UserSubscriptionResponse purchaseSubscription(Long userId, PurchaseSubscriptionRequest request);
    
    void cancelSubscription(Long userId, Long subscriptionId);
    
    UserSubscriptionResponse getActiveSubscription(Long userId);
    
    List<UserSubscriptionResponse> getUserSubscriptions(Long userId);
    
    UserQuotaResponse getUserQuota(Long userId);
    
    boolean checkProjectQuota(Long userId);
    
    /**
     * Check if user has an active subscription (for collaboration feature)
     * @param userId User ID to check
     * @return true if user has active subscription, false otherwise
     */
    boolean hasActiveSubscription(Long userId);
    
    void processExpiredSubscriptions();
}
