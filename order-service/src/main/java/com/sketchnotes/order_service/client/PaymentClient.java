package com.sketchnotes.order_service.client;

import com.sketchnotes.order_service.dtos.PaymentRequestDTO;
import com.sketchnotes.order_service.dtos.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${payment-service.url:http://localhost:8081}")
    private String paymentServiceUrl;

    /**
     * Tạo payment link thông qua payment-service
     */
    public PaymentResponseDTO createPaymentLink(PaymentRequestDTO paymentRequest) {
        try {
            String url = paymentServiceUrl + "/api/payments/create";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<PaymentRequestDTO> request = new HttpEntity<>(paymentRequest, headers);
            
            ResponseEntity<PaymentResponseDTO> response = restTemplate.postForEntity(
                    url, request, PaymentResponseDTO.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
            throw new RuntimeException("Failed to create payment link");
            
        } catch (Exception e) {
            log.error("Error creating payment link for order {}: {}", 
                    paymentRequest.getOrderId(), e.getMessage());
            throw new RuntimeException("Failed to create payment link: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra trạng thái payment thông qua payment-service
     */
    public PaymentResponseDTO getPaymentStatus(String orderCode) {
        try {
            String url = paymentServiceUrl + "/api/payments/status/" + orderCode;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<PaymentResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PaymentResponseDTO.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
            throw new RuntimeException("Failed to get payment status");
            
        } catch (Exception e) {
            log.error("Error getting payment status for orderCode {}: {}", orderCode, e.getMessage());
            throw new RuntimeException("Failed to get payment status: " + e.getMessage());
        }
    }

    /**
     * Hủy payment link thông qua payment-service
     */
    public boolean cancelPaymentLink(String orderCode) {
        try {
            String url = paymentServiceUrl + "/api/payments/cancel/" + orderCode;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Boolean.class);
            
            return response.getStatusCode() == HttpStatus.OK && 
                   response.getBody() != null && response.getBody();
            
        } catch (Exception e) {
            log.error("Error canceling payment link for orderCode {}: {}", orderCode, e.getMessage());
            return false;
        }
    }
}
