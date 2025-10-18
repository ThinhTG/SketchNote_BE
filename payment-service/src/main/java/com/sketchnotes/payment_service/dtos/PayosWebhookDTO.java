package com.sketchnotes.payment_service.dtos;

import lombok.Data;

@Data
public class PayosWebhookDTO {
    private String code;
    private String desc;
    private Boolean success;
    private PayosWebhookDataDTO data;
    private String signature;
}