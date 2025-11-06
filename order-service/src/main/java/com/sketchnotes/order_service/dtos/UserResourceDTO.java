package com.sketchnotes.order_service.dtos;

public class UserResourceDTO {
    private  Long userId;
    private  Long resourceTemplateId;
    public UserResourceDTO(Long userId, Long resourceTemplateId, Long orderId) {
        this.userId = userId;
    }
}
