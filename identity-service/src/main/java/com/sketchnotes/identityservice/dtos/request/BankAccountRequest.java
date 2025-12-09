package com.sketchnotes.identityservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountRequest {
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;
    
    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    private String accountNumber;
    
    @NotBlank(message = "Account holder name is required")
    @Size(max = 100, message = "Account holder name must not exceed 100 characters")
    private String accountHolderName;
    
    @NotBlank(message = "Logo URL is required")
    @Size(max = 255, message = "Logo URL must not exceed 255 characters")
    private String logoUrl;

    @Size(max = 100, message = "Branch must not exceed 100 characters")
    private String branch;
    
    private Boolean isDefault;
}
