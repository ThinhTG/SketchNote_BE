package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.events.PaymentFailedEvent;
import com.sketchnotes.order_service.events.PaymentSucceededEvent;

/**
 * Service interface for payment operations.
 * Handles payment success and failure events from payment gateway callbacks.
 */
public interface IPaymentService {
    
    /**
     * Handle successful payment event.
     * Creates user resources and deposits to designer wallets.
     * @param event Payment success event containing order information
     */
    void handlePaymentSuccess(PaymentSucceededEvent event);
    
    /**
     * Handle failed payment event.
     * Updates order status to failed.
     * @param event Payment failure event containing order and failure information
     */
    void handlePaymentFailed(PaymentFailedEvent event);
}
