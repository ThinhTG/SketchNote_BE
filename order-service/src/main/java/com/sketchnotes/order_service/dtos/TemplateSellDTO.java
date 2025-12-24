package com.sketchnotes.order_service.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TemplateSellDTO {
    private String name;          // optional
    private String description;   // optional
    private String type;          // required
    private BigDecimal price;     // required
}

