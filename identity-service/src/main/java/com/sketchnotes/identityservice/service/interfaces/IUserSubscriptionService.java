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
    
    void processExpiredSubscriptions();
}
