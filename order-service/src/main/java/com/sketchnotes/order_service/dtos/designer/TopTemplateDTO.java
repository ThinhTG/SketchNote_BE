package com.sketchnotes.order_service.dtos.designer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopTemplateDTO {
    private Long templateId;
    private String templateName;
    private Long soldCount;
    private BigDecimal revenue;
}
