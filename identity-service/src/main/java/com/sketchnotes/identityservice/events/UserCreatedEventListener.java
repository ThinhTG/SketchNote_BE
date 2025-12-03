package com.sketchnotes.identityservice.events;

import com.sketchnotes.identityservice.service.interfaces.ICreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener xử lý event khi user mới được tạo
 * Tự động tặng credit miễn phí cho user mới
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventListener {
    
    private final ICreditService creditService;
    
    private static final Integer INITIAL_FREE_CREDITS = 50;
    
    @EventListener
    @Async
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        try {
            log.info("Handling UserCreatedEvent for user: {} ({})", event.getUserId(), event.getEmail());
            
            // Tặng credit miễn phí cho user mới
            creditService.grantInitialCredits(event.getUserId(), INITIAL_FREE_CREDITS);
            
            log.info("Successfully granted {} initial credits to user: {}", 
                    INITIAL_FREE_CREDITS, event.getUserId());
                    
        } catch (Exception ex) {
            log.error("Failed to grant initial credits for user: {}", event.getUserId(), ex);
            // Không throw exception để không ảnh hưởng đến flow chính
        }
    }
}
