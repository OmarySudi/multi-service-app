package com.multiservice.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private BigDecimal amount;
    private String currency;
    private String phoneNumber;
    private String reference;
    private String status; // pending, completed, failed, refunded
    private LocalDateTime createdAt;
}
