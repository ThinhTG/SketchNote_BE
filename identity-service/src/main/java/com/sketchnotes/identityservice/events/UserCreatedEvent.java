package com.sketchnotes.identityservice.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event được publish khi user mới được tạo
 */
@Getter
public class UserCreatedEvent extends ApplicationEvent {
    
    private final Long userId;
    private final String email;
    
    public UserCreatedEvent(Object source, Long userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }
}
