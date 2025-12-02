package com.sketchnotes.identityservice.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho việc mua credit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseCreditRequest {
    
    @NotNull(message = "Amount is required")
    @Min(value = 100, message = "Minimum purchase is 100 credits")
    private Integer amount;  // so luong credits muon mua ( 1 credit = 1k vnd)
}
