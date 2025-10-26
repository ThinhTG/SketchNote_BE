package com.sketchnotes.order_service.dtos;

import lombok.Builder;
import lombok.Data;

@Data
public class DesignerInfoDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
}
