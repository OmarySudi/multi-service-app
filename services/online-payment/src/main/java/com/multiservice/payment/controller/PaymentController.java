package com.multiservice.payment.controller;

import com.multiservice.payment.dto.PaymentInitiateRequest;
import com.multiservice.payment.dto.PaymentResponse;
import com.multiservice.payment.dto.RefundRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.multiservice.common.constants.Permissions.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    @PostMapping("/initiate")
    @PreAuthorize("hasAuthority('" + PAYMENT_INITIATE + "')")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestBody PaymentInitiateRequest request,
            Authentication auth) {
        // Mock response
        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .phoneNumber(request.getPhoneNumber())
                .reference(request.getReference())
                .status("pending")
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PAYMENT_VIEW + "')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        // Mock response
        List<PaymentResponse> payments = new ArrayList<>();
        payments.add(PaymentResponse.builder()
                .id(1L)
                .amount(new BigDecimal("50000"))
                .currency("TZS")
                .phoneNumber("+255712345678")
                .reference("PAY-001")
                .status("completed")
                .createdAt(LocalDateTime.now().minusHours(3))
                .build());
        payments.add(PaymentResponse.builder()
                .id(2L)
                .amount(new BigDecimal("25000"))
                .currency("TZS")
                .phoneNumber("+255723456789")
                .reference("PAY-002")
                .status("pending")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PAYMENT_VIEW + "')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        // Mock response
        PaymentResponse response = PaymentResponse.builder()
                .id(id)
                .amount(new BigDecimal("50000"))
                .currency("TZS")
                .phoneNumber("+255712345678")
                .reference("PAY-" + id)
                .status("completed")
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('" + PAYMENT_REFUND + "')")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @RequestBody RefundRequest request) {
        // Mock response
        PaymentResponse response = PaymentResponse.builder()
                .id(id)
                .amount(request.getAmount())
                .currency("TZS")
                .phoneNumber("+255712345678")
                .reference("REF-" + id)
                .status("refunded")
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report")
    @PreAuthorize("hasAuthority('" + PAYMENT_REPORT + "')")
    public ResponseEntity<String> getPaymentReport() {
        return ResponseEntity.ok(
                "Payment Report: Total Transactions=1000, Completed=950, Pending=30, Failed=20, Total Amount=TZS 50,000,000"
        );
    }

    @PostMapping("/reconcile")
    @PreAuthorize("hasAuthority('" + PAYMENT_RECONCILE + "')")
    public ResponseEntity<String> reconcilePayments() {
        return ResponseEntity.ok("Payment reconciliation completed successfully");
    }

    @GetMapping("/public/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Online Payment Service is running!");
    }
}
